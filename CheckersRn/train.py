import json
import os

import torch
import torch.nn as nn
import torch.optim as optim

from ai import CheckersNet, encode_board, move_to_id


DATASET_FILE = "dataset.json"
MODEL_DIR = "models"
MODEL_FILE = os.path.join(MODEL_DIR, "ai_v001.pt")

EPOCHS = 100
LEARNING_RATE = 0.001


def load_dataset():
    with open(DATASET_FILE, "r", encoding="utf-8") as f:
        return json.load(f)


def train():
    dataset = load_dataset()

    if not dataset:
        raise RuntimeError("Dataset is empty")

    model = CheckersNet()

    optimizer = optim.Adam(
        model.parameters(),
        lr=LEARNING_RATE
    )

    criterion = nn.CrossEntropyLoss()

    model.train()

    for epoch in range(EPOCHS):

        total_loss = 0.0
        correct = 0
        total = 0

        for sample in dataset:

            board = sample["board"]
            player = sample["player"]
            legal_moves = sample["legalMoves"]
            best_move = sample["bestMove"]

            # Convert board to [1, 5, 8, 8]
            state = encode_board(
                board,
                player
            )

            # Network prediction
            policy, value = model(state)

            scores = policy[0]

            # Only legal moves are allowed
            masked_scores = torch.full_like(
                scores,
                float("-inf")
            )

            legal_ids = []

            for move in legal_moves:

                mid = move_to_id(move)

                masked_scores[mid] = scores[mid]

                legal_ids.append(mid)

            # Target = Minimax move
            target_id = move_to_id(best_move)

            target = torch.tensor(
                [target_id],
                dtype=torch.long
            )

            # Cross entropy over the 4096 move classes
            loss = criterion(
                scores.unsqueeze(0),
                target
            )

            optimizer.zero_grad()
            loss.backward()
            optimizer.step()

            total_loss += loss.item()

            predicted_id = torch.argmax(
                masked_scores
            ).item()

            if predicted_id == target_id:
                correct += 1

            total += 1

        accuracy = correct / total

        if epoch == 0 or (epoch + 1) % 10 == 0:
            print(
                f"Epoch {epoch + 1:3d}/{EPOCHS} "
                f"loss={total_loss:.6f} "
                f"accuracy={accuracy:.2%}"
            )

    os.makedirs(MODEL_DIR, exist_ok=True)

    torch.save(
        model.state_dict(),
        MODEL_FILE
    )

    print()
    print(f"Saved model: {MODEL_FILE}")


if __name__ == "__main__":
    train()