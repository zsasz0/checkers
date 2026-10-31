import subprocess
import json


request = {
    "board": [
        [0,0,0,0,0,0,0,0],
        [0,0,1,0,1,0,1,0],
        [0,1,0,1,0,1,0,1],
        [0,0,0,0,0,0,0,0],
        [0,0,0,2,0,0,0,0],
        [0,2,0,0,0,2,0,0],
        [2,0,2,0,2,0,2,0],
        [0,0,0,0,0,0,0,0]
    ],
    "player": 1,
    "legalMoves": [
        {"from": 17, "to": 26},
        {"from": 21, "to": 30}
    ]
}


process = subprocess.Popen(
    ["python", "ai.py"],
    stdin=subprocess.PIPE,
    stdout=subprocess.PIPE,
    stderr=subprocess.PIPE,
    text=True
)

# Send request
process.stdin.write(json.dumps(request) + "\n")
process.stdin.flush()

# Read response
response = process.stdout.readline()

print("STDOUT:")
print(repr(response))

# Check stderr
error = process.stderr.read()

print("\nSTDERR:")
print(error)

print("\nRETURN CODE:")
print(process.poll())