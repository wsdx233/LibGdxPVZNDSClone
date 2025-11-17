#!/usr/bin/env python3
"""
Generate encrypted flag data for FlagScreen.kt
Flag: flag{BECAUSE_I_AM_CRAAAZY}
"""

def main():
    flag = "flag{BECAUSE_I_AM_CRAAAZY}"

    print(f"Original flag: {flag}")
    print(f"Flag length: {len(flag)}")
    print()

    # Simple XOR encryption with two different keys
    xor_key1 = 0x66
    xor_key2 = 0x77

    # Split flag into two chunks
    mid = len(flag) // 2
    chunk1 = flag[:mid]
    chunk2 = flag[mid:]

    print(f"Chunk 1: '{chunk1}' (length: {len(chunk1)})")
    print(f"Chunk 2: '{chunk2}' (length: {len(chunk2)})")
    print()

    # Encrypt chunk 1 with key1
    encrypted1 = [ord(c) ^ xor_key1 for c in chunk1]
    print("Encrypted chunk 1 (XOR with 0x66):")
    print("byteArrayOf(")
    for i, byte in enumerate(encrypted1):
        if i > 0 and i % 8 == 0:
            print()
        print(f"    0x{byte:02x},", end=" " if (i + 1) % 8 != 0 else "\n")
    print("\n)")
    print()

    # Encrypt chunk 2 with key2
    encrypted2 = [ord(c) ^ xor_key2 for c in chunk2]
    print("Encrypted chunk 2 (XOR with 0x77):")
    print("byteArrayOf(")
    for i, byte in enumerate(encrypted2):
        if i > 0 and i % 8 == 0:
            print()
        print(f"    0x{byte:02x},", end=" " if (i + 1) % 8 != 0 else "\n")
    print("\n)")
    print()

    # Verify decryption
    decrypted1 = ''.join(chr(b ^ xor_key1) for b in encrypted1)
    decrypted2 = ''.join(chr(b ^ xor_key2) for b in encrypted2)
    reconstructed = decrypted1 + decrypted2

    print(f"Verification - Decrypted: '{reconstructed}'")
    print(f"Match: {reconstructed == flag}")
    print()

    # Generate byte array for direct reconstruction (fallback method)
    print("Direct byte array (for reconstructFlag method):")
    print("Prefix 'flag{': byteArrayOf(0x66, 0x6c, 0x61, 0x67, 0x7b)")
    print("Suffix '}': byteArrayOf(0x7d)")
    print()

    # Show hex values for each character
    print("Character breakdown:")
    for i, c in enumerate(flag):
        print(f"  {i:2d}. '{c}' = 0x{ord(c):02x} ({ord(c)})")

if __name__ == "__main__":
    main()
