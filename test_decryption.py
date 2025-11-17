#!/usr/bin/env python3
"""
Test the decryption logic to ensure it matches the Kotlin implementation
"""

def xor_decrypt(data_bytes, key):
    """XOR decryption"""
    return bytes([b ^ key for b in data_bytes])

def simple_aes_decrypt(data_bytes, key_bytes):
    """Simple XOR encryption simulating AES (symmetric)"""
    result = []
    for i, byte in enumerate(data_bytes):
        result.append(byte ^ key_bytes[i % len(key_bytes)])
    return bytes(result)

def rotate_decrypt(text, offset):
    """Rotation cipher decryption (reverse Caesar cipher)"""
    result = []
    for char in text:
        if char.isupper():
            shifted = (ord(char) - ord('A') - offset + 26) % 26
            result.append(chr(ord('A') + shifted))
        elif char.islower():
            shifted = (ord(char) - ord('a') - offset + 26) % 26
            result.append(chr(ord('a') + shifted))
        else:
            result.append(char)
    return ''.join(result)

def substitution_decrypt(text, reverse_map):
    """Substitution cipher decryption"""
    return ''.join(reverse_map.get(c, c) for c in text)

def main():
    print("Testing Kotlin decryption logic...")
    print("=" * 70)
    print()

    # Encrypted data from Kotlin
    encrypted_chunk1 = bytes([
        0x56, 0x91, 0xd0, 0x78, 0x13, 0x82, 0x40, 0xeb,
        0x31, 0xbd, 0x1a, 0xda, 0x1a
    ])

    encrypted_chunk2 = bytes([
        0x98, 0x27, 0x82, 0x65, 0xc7, 0xed, 0x4d, 0x12,
        0x89, 0x54, 0xe4, 0x23, 0xa3
    ])

    xor_key1 = 0x66
    xor_key2 = 0x77

    aes_key = bytes([
        0x4a, 0x91, 0xc3, 0x7f, 0x2e, 0xb5, 0x68, 0xd4,
        0x1c, 0x89, 0x3a, 0xf2, 0x5d, 0xa6, 0x71, 0xbe
    ])

    # Substitution map (same as Kotlin)
    substitution_map = {
        'A': 'Q', 'B': 'W', 'C': 'E', 'D': 'R', 'E': 'T',
        'F': 'Y', 'G': 'U', 'H': 'I', 'I': 'O', 'J': 'P',
        'K': 'A', 'L': 'S', 'M': 'D', 'N': 'F', 'O': 'G',
        'P': 'H', 'Q': 'J', 'R': 'K', 'S': 'L', 'T': 'Z',
        'U': 'X', 'V': 'C', 'W': 'V', 'X': 'B', 'Y': 'N',
        'Z': 'M', '_': '!', '{': '[', '}': ']'
    }
    reverse_map = {v: k for k, v in substitution_map.items()}

    # Rotation offset
    seed = "PLANTS_VS_ZOMBIES_2025"
    rotation_offset = sum(ord(c) for c in seed) % 26

    print("Step 1: XOR decrypt chunks")
    decrypted1 = xor_decrypt(encrypted_chunk1, xor_key1)
    decrypted2 = xor_decrypt(encrypted_chunk2, xor_key2)
    print(f"  Chunk 1: {[hex(b) for b in decrypted1]}")
    print(f"  Chunk 2: {[hex(b) for b in decrypted2]}")
    print()

    print("Step 2: Combine chunks")
    combined = decrypted1 + decrypted2
    print(f"  Combined: {[hex(b) for b in combined]}")
    print()

    print("Step 3: AES-like decryption")
    aes_decrypted = simple_aes_decrypt(combined, aes_key)
    print(f"  Result: {[hex(b) for b in aes_decrypted]}")
    print()

    print("Step 4: Convert to string")
    intermediate = aes_decrypted.decode('utf-8')
    print(f"  String: '{intermediate}'")
    print()

    print("Step 5: Rotation cipher decryption")
    print(f"  Offset: {rotation_offset}")
    rotated = rotate_decrypt(intermediate, rotation_offset)
    print(f"  Result: '{rotated}'")
    print()

    print("Step 6: Substitution cipher decryption")
    final = substitution_decrypt(rotated, reverse_map)
    print(f"  Result: '{final}'")
    print()

    print("=" * 70)
    expected = "flag{BECAUSE_I_AM_CRAAAZY}"
    if final == expected:
        print("✓ SUCCESS! Decryption works correctly!")
        print(f"  Final flag: {final}")
    else:
        print("✗ FAILED! Decryption mismatch!")
        print(f"  Expected: {expected}")
        print(f"  Got:      {final}")
    print("=" * 70)

if __name__ == "__main__":
    main()
