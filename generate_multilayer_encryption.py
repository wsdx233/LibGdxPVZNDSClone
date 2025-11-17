#!/usr/bin/env python3
"""
Generate multi-layer encrypted flag data for FlagScreen.kt
Flag: flag{BECAUSE_I_AM_CRAAAZY}

Encryption layers (in reverse order of decryption):
1. Start with plaintext flag
2. Apply substitution cipher
3. Apply rotation cipher (Caesar cipher)
4. Apply simple AES-like XOR with key
5. Split into two chunks and XOR with different keys
"""

def substitution_encrypt(text, sub_map):
    """Apply substitution cipher"""
    return ''.join(sub_map.get(c, c) for c in text)

def rotation_encrypt(text, offset):
    """Apply Caesar cipher rotation"""
    result = []
    for char in text:
        if char.isupper():
            shifted = (ord(char) - ord('A') + offset) % 26
            result.append(chr(ord('A') + shifted))
        elif char.islower():
            shifted = (ord(char) - ord('a') + offset) % 26
            result.append(chr(ord('a') + shifted))
        else:
            result.append(char)
    return ''.join(result)

def simple_aes_encrypt(data_bytes, key_bytes):
    """Simple XOR encryption simulating AES"""
    result = []
    for i, byte in enumerate(data_bytes):
        result.append(byte ^ key_bytes[i % len(key_bytes)])
    return bytes(result)

def xor_encrypt(data_bytes, key):
    """XOR encryption with single key"""
    return bytes([b ^ key for b in data_bytes])

def main():
    flag = "flag{BECAUSE_I_AM_CRAAAZY}"
    print(f"Original flag: {flag}")
    print(f"Flag length: {len(flag)}")
    print("=" * 70)
    print()

    # Define substitution map (same as in Kotlin code)
    substitution_map = {
        'A': 'Q', 'B': 'W', 'C': 'E', 'D': 'R', 'E': 'T',
        'F': 'Y', 'G': 'U', 'H': 'I', 'I': 'O', 'J': 'P',
        'K': 'A', 'L': 'S', 'M': 'D', 'N': 'F', 'O': 'G',
        'P': 'H', 'Q': 'J', 'R': 'K', 'S': 'L', 'T': 'Z',
        'U': 'X', 'V': 'C', 'W': 'V', 'X': 'B', 'Y': 'N',
        'Z': 'M', '_': '!', '{': '[', '}': ']'
    }

    # Calculate rotation offset (same as Kotlin: "PLANTS_VS_ZOMBIES_2025")
    seed = "PLANTS_VS_ZOMBIES_2025"
    rotation_offset = sum(ord(c) for c in seed) % 26
    print(f"Rotation offset: {rotation_offset}")
    print()

    # Layer 1: Apply substitution cipher
    step1 = substitution_encrypt(flag, substitution_map)
    print(f"After substitution: {step1}")
    print()

    # Layer 2: Apply rotation cipher
    step2 = rotation_encrypt(step1, rotation_offset)
    print(f"After rotation (offset={rotation_offset}): {step2}")
    print()

    # Layer 3: Convert to bytes for AES-like encryption
    step3_bytes = step2.encode('utf-8')
    print(f"As bytes: {[hex(b) for b in step3_bytes]}")
    print()

    # Generate AES key (16 bytes)
    aes_key = bytes([
        0x4a, 0x91, 0xc3, 0x7f, 0x2e, 0xb5, 0x68, 0xd4,
        0x1c, 0x89, 0x3a, 0xf2, 0x5d, 0xa6, 0x71, 0xbe
    ])
    print(f"AES key: {[hex(b) for b in aes_key]}")
    print()

    # Layer 4: Apply simple AES-like encryption
    step4 = simple_aes_encrypt(step3_bytes, aes_key)
    print(f"After AES-like encryption: {[hex(b) for b in step4]}")
    print()

    # Layer 5: Split into two chunks
    mid = len(step4) // 2
    chunk1 = step4[:mid]
    chunk2 = step4[mid:]
    print(f"Chunk 1 length: {len(chunk1)}")
    print(f"Chunk 2 length: {len(chunk2)}")
    print()

    # Layer 6: XOR each chunk with different keys
    xor_key1 = 0x66
    xor_key2 = 0x77

    encrypted_chunk1 = xor_encrypt(chunk1, xor_key1)
    encrypted_chunk2 = xor_encrypt(chunk2, xor_key2)

    print("=" * 70)
    print("FINAL ENCRYPTED DATA FOR KOTLIN:")
    print("=" * 70)
    print()

    print("// Encrypted chunk 1 (XOR with 0x66):")
    print("private val encryptedChunk1 = byteArrayOf(")
    for i, byte in enumerate(encrypted_chunk1):
        if i % 8 == 0:
            print("    ", end="")
        print(f"0x{byte:02x}", end="")
        if i < len(encrypted_chunk1) - 1:
            print(", ", end="")
        if (i + 1) % 8 == 0 and i < len(encrypted_chunk1) - 1:
            print()
    print("\n)")
    print()

    print("// Encrypted chunk 2 (XOR with 0x77):")
    print("private val encryptedChunk2 = byteArrayOf(")
    for i, byte in enumerate(encrypted_chunk2):
        if i % 8 == 0:
            print("    ", end="")
        print(f"0x{byte:02x}", end="")
        if i < len(encrypted_chunk2) - 1:
            print(", ", end="")
        if (i + 1) % 8 == 0 and i < len(encrypted_chunk2) - 1:
            print()
    print("\n)")
    print()

    print("// AES encryption key:")
    print("private val aesEncryptedKey = byteArrayOf(")
    for i, byte in enumerate(aes_key):
        if i % 8 == 0:
            print("    ", end="")
        print(f"0x{byte:02x}.toByte()", end="")
        if i < len(aes_key) - 1:
            print(", ", end="")
        if (i + 1) % 8 == 0 and i < len(aes_key) - 1:
            print()
    print("\n)")
    print()

    # Verify decryption
    print("=" * 70)
    print("VERIFICATION:")
    print("=" * 70)
    print()

    # Reverse the process
    verify_chunk1 = xor_encrypt(encrypted_chunk1, xor_key1)
    verify_chunk2 = xor_encrypt(encrypted_chunk2, xor_key2)
    verify_combined = verify_chunk1 + verify_chunk2
    print(f"After XOR decryption: {[hex(b) for b in verify_combined]}")

    verify_aes = simple_aes_encrypt(verify_combined, aes_key)  # XOR is symmetric
    print(f"After AES decryption: {[hex(b) for b in verify_aes]}")

    verify_str = verify_aes.decode('utf-8')
    print(f"After bytes->string: {verify_str}")

    # Reverse rotation
    verify_rotated = rotation_encrypt(verify_str, -rotation_offset)  # Negative offset to decrypt
    print(f"After rotation decryption: {verify_rotated}")

    # Reverse substitution
    reverse_sub_map = {v: k for k, v in substitution_map.items()}
    verify_final = substitution_encrypt(verify_rotated, reverse_sub_map)
    print(f"Final decrypted: {verify_final}")
    print()

    print(f"Match: {verify_final == flag}")
    print()

    # Also print the rotation offset calculation for Kotlin
    print("=" * 70)
    print("ROTATION OFFSET CALCULATION:")
    print("=" * 70)
    print(f'Seed string: "{seed}"')
    print(f"Sum of character codes: {sum(ord(c) for c in seed)}")
    print(f"Rotation offset (sum % 26): {rotation_offset}")

if __name__ == "__main__":
    main()
