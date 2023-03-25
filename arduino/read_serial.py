import argparse
import serial

def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("port")
    parser.add_argument("--rate", default=115200, type=int)
    return parser.parse_args();

def main():
    """Main."""
    args = parse_args()

    with serial.Serial(args.port, baudrate=int(args.rate), timeout=1) as ser:
        print("Connected to port: ", ser.name)
        while True:
            line = ser.readline().decode().strip("\n\r ")
            print(line)

if __name__ == "__main__":
    main()
