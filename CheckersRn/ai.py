import sys
import json
import os

import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim


# ============================================================
# Files
# ============================================================

BASE_MODEL_FILE = os.path.join(
    "models",
    "ai_v001.pt"
)

SUPERVISED_MODEL_FILE = os.path.join(
    "models",
    "ai_supervised_latest.pt"
)


# ============================================================
# Training
# ============================================================

LEARNING_RATE = 0.0001

SAVE_EVERY = 100


# ============================================================
# Neural Network
# ============================================================

class ResidualBlock(nn.Module):

    def __init__(self, channels: int):
        super().__init__()

        self.conv1 = nn.Conv2d(
            channels,
            channels,
            kernel_size=3,
            padding=1
        )

        self.bn1 = nn.BatchNorm2d(
            channels
        )

        self.conv2 = nn.Conv2d(
            channels,
            channels,
            kernel_size=3,
            padding=1
        )

        self.bn2 = nn.BatchNorm2d(
            channels
        )

    def forward(self, x):

        identity = x

        x = self.conv1(x)
        x = self.bn1(x)
        x = F.relu(x)

        x = self.conv2(x)
        x = self.bn2(x)

        x = x + identity

        return F.relu(x)


class CheckersNet(nn.Module):

    def __init__(self):
        super().__init__()

        self.input = nn.Sequential(
            nn.Conv2d(
                5,
                64,
                kernel_size=3,
                padding=1
            ),
            nn.BatchNorm2d(64),
            nn.ReLU()
        )

        self.res_blocks = nn.Sequential(
            ResidualBlock(64),
            ResidualBlock(64),
            ResidualBlock(64)
        )

        self.policy = nn.Sequential(
            nn.Flatten(),
            nn.Linear(
                64 * 8 * 8,
                4096
            )
        )

        self.value = nn.Sequential(
            nn.Flatten(),
            nn.Linear(
                64 * 8 * 8,
                128
            ),
            nn.ReLU(),
            nn.Linear(
                128,
                1
            ),
            nn.Tanh()
        )

    def forward(self, x):

        x = self.input(x)

        x = self.res_blocks(x)

        policy = self.policy(x)

        value = self.value(x)

        return policy, value


# ============================================================
# Board encoding
#
# 0 = empty
# 1 = WHITE normal
# 2 = BLACK normal
# 3 = WHITE king
# 4 = BLACK king
# ============================================================

def encode_board(board, player):

    x = torch.zeros(
        (5, 8, 8),
        dtype=torch.float32
    )

    for row in range(8):

        for col in range(8):

            piece = board[row][col]

            if piece == 1:
                x[0, row, col] = 1.0

            elif piece == 3:
                x[1, row, col] = 1.0

            elif piece == 2:
                x[2, row, col] = 1.0

            elif piece == 4:
                x[3, row, col] = 1.0

    x[4, :, :] = float(player)

    return x.unsqueeze(0)


# ============================================================
# Move encoding
# ============================================================

def move_to_id(move):

    return (
        move["from"] * 64
        + move["to"]
    )


# ============================================================
# Model validation
# ============================================================

def model_is_finite(model):

    for parameter in model.parameters():

        if not torch.isfinite(
                parameter
        ).all():

            return False

    return True


# ============================================================
# Load model
# ============================================================

def load_model():

    model = CheckersNet()

    if os.path.exists(
            SUPERVISED_MODEL_FILE):

        model_file = (
            SUPERVISED_MODEL_FILE
        )

    elif os.path.exists(
            BASE_MODEL_FILE):

        model_file = (
            BASE_MODEL_FILE
        )

    else:

        print(
            "MODEL_NOT_FOUND",
            file=sys.stderr,
            flush=True
        )

        return None

    try:

        state = torch.load(
            model_file,
            map_location="cpu"
        )

        model.load_state_dict(
            state
        )

        if not model_is_finite(
                model):

            print(
                "MODEL_INVALID",
                file=sys.stderr,
                flush=True
            )

            return None

        print(
            f"MODEL_LOADED {model_file}",
            file=sys.stderr,
            flush=True
        )

    except Exception as e:

        print(
            f"MODEL_LOAD_ERROR {e}",
            file=sys.stderr,
            flush=True
        )

        return None

    model.train()

    return model


# ============================================================
# Train on Minimax teacher
# ============================================================

def train_on_minimax(
        model,
        optimizer,
        board,
        player,
        legal_moves,
        teacher_move):

    state = encode_board(
        board,
        player
    )

    policy, _ = model(
        state
    )

    scores = policy[0]

    legal_ids = [
        move_to_id(move)
        for move in legal_moves
    ]

    if not legal_ids:

        raise ValueError(
            "No legal moves"
        )

    teacher_id = move_to_id(
        teacher_move
    )

    if teacher_id not in legal_ids:

        raise ValueError(
            "Teacher move is not legal"
        )

    legal_scores = scores[
        torch.tensor(
            legal_ids,
            dtype=torch.long
        )
    ]

    teacher_index = legal_ids.index(
        teacher_id
    )

    target = torch.tensor(
        [teacher_index],
        dtype=torch.long
    )

    loss = F.cross_entropy(
        legal_scores.unsqueeze(0),
        target
    )

    if not torch.isfinite(loss):

        raise RuntimeError(
            "Training loss is not finite"
        )

    optimizer.zero_grad()

    loss.backward()

    for parameter in model.parameters():

        if parameter.grad is None:
            continue

        if not torch.isfinite(
                parameter.grad
        ).all():

            optimizer.zero_grad()

            raise RuntimeError(
                "Invalid gradient"
            )

    torch.nn.utils.clip_grad_norm_(
        model.parameters(),
        1.0
    )

    optimizer.step()

    if not model_is_finite(
            model):

        raise RuntimeError(
            "Model became non-finite"
        )

    return loss.item()


# ============================================================
# Choose best legal move
# ============================================================

def choose_best_move(
        model,
        board,
        player,
        legal_moves):

    if not legal_moves:

        raise ValueError(
            "No legal moves"
        )

    state = encode_board(
        board,
        player
    )

    with torch.no_grad():

        policy, value = model(
            state
        )

    scores = policy[0]

    legal_ids = [
        move_to_id(move)
        for move in legal_moves
    ]

    legal_scores = scores[
        torch.tensor(
            legal_ids,
            dtype=torch.long
        )
    ]

    if not torch.isfinite(
            legal_scores
    ).all():

        raise RuntimeError(
            "Invalid neural-network scores"
        )

    best_index = torch.argmax(
        legal_scores
    ).item()

    return (
        legal_moves[best_index],
        value.item()
    )


# ============================================================
# Main
# ============================================================

def main():

    model = load_model()

    if model is None:
        return

    optimizer = optim.Adam(
        model.parameters(),
        lr=LEARNING_RATE
    )

    training_steps = 0

    # ========================================================
    # Teacher agreement statistics
    # ========================================================

    teacher_matches = 0
    teacher_total = 0

    print(
        "AI_READY",
        file=sys.stderr,
        flush=True
    )

    # ========================================================
    # Persistent Java <-> Python loop
    # ========================================================

    for line in sys.stdin:

        line = line.strip()

        if not line:
            continue

        try:

            request = json.loads(
                line
            )

            message_type = request.get(
                "type",
                "play"
            )

            # =================================================
            # PLAY + SUPERVISED TRAINING
            # =================================================

            if message_type == "play":

                board = request[
                    "board"
                ]

                player = request[
                    "player"
                ]

                legal_moves = request[
                    "legalMoves"
                ]

                teacher_move = request[
                    "teacherMove"
                ]

                # ------------------------------------------------
                # 1. Train on Minimax teacher
                # ------------------------------------------------

                loss = train_on_minimax(
                    model,
                    optimizer,
                    board,
                    player,
                    legal_moves,
                    teacher_move
                )

                training_steps += 1

                # ------------------------------------------------
                # 2. Python chooses its current best move
                # ------------------------------------------------

                move, value = choose_best_move(
                    model,
                    board,
                    player,
                    legal_moves
                )

                # ------------------------------------------------
                # 3. Teacher agreement
                # ------------------------------------------------

                teacher_total += 1

                if (
                        move_to_id(move)
                        ==
                        move_to_id(teacher_move)
                ):

                    teacher_matches += 1

                teacher_agreement = (
                    teacher_matches
                    / teacher_total
                    if teacher_total > 0
                    else 0.0
                )

                # ------------------------------------------------
                # 4. Save periodically
                # ------------------------------------------------

                if (
                        training_steps
                        % SAVE_EVERY
                        == 0
                ):

                    os.makedirs(
                        "models",
                        exist_ok=True
                    )

                    torch.save(
                        model.state_dict(),
                        SUPERVISED_MODEL_FILE
                    )

                    print(
                        f"[TRAIN] Saved model after "
                        f"{training_steps} steps",
                        file=sys.stderr,
                        flush=True
                    )

                # ------------------------------------------------
                # 5. Print metrics
                # ------------------------------------------------

                print(
                    f"[TRAIN] Steps: "
                    f"{training_steps} "
                    f"Teacher agreement: "
                    f"{teacher_agreement:.2%} "
                    f"({teacher_matches}/"
                    f"{teacher_total})",
                    file=sys.stderr,
                    flush=True
                )

                # ------------------------------------------------
                # 6. JSON response
                # ------------------------------------------------

                response = {
                    "move": move,
                    "value": value,
                    "trainingLoss": loss,
                    "trainingSteps": training_steps,
                    "teacherMatches": teacher_matches,
                    "teacherTotal": teacher_total,
                    "teacherAgreement": teacher_agreement
                }

                print(
                    json.dumps(
                        response
                    ),
                    flush=True
                )

            else:

                raise ValueError(
                    f"Unknown message type: "
                    f"{message_type}"
                )

        except Exception as e:

            print(
                json.dumps({
                    "error": str(e)
                }),
                flush=True
            )

    # ========================================================
    # Save when Python exits
    # ========================================================

    if model_is_finite(model):

        os.makedirs(
            "models",
            exist_ok=True
        )

        torch.save(
            model.state_dict(),
            SUPERVISED_MODEL_FILE
        )

        print(
            "[TRAIN] Final model saved.",
            file=sys.stderr,
            flush=True
        )


if __name__ == "__main__":

    main()