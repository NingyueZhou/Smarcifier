import argparse
import serial
import numpy as np
import time

class History():
    def __init__(self, size=10, default_elem=None):
        self._buf = [default_elem for _ in range(size)]
        self._index = 0

    def push(self, item):
        self._buf[self._index] = item
        self._index = (self._index + 1) % len(self._buf)

    def variance(self):
        """Calculate the variance over all items in the history."""
        return np.var(self._buf)

def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("port")
    parser.add_argument("--rate", default=115200, type=int)
    return parser.parse_args()

def main():
    """Main."""
    args = parse_args()
    history = History(10, 0.0)

    with serial.Serial(args.port, baudrate=int(args.rate), timeout=1) as ser:
        print("Connected to port: ", ser.name)
        start = time.time()
        while True:
            line = ser.readline().decode().strip("\n\r ")
            if not line:
                continue

            print(line)

            # Calculate variance and decide when to stop measuring
            history.push(float(line))
            if history.variance() < 2.0e-5:
                break

        end = time.time()
        print(
            f"\nTemperature measurement complete! ({end-start:.1f} seconds)",
            end=""
        )

if __name__ == "__main__":
    main()
