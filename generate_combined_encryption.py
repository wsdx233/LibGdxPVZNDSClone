#!/usr/bin/env python3
"""
Generate combined encryption for FlagScreen
This script generates the killCountEncryptedFlag that contains both chunks
"""

def derive_key_from_kill_count(kill_count):
    """
    Derive encryption key from kill count using the same algorithm as Kotlin
    """
    # Layer 1: Apply rounding transformation
    rounded = ((kill_count + 5) // 10) * 10

    # Layer 2: Hash-like transformation using prime numbers
    hash1 = (rounded * 31 + 17) % 997
    hash2 = (rounded * 37 + 23) % 991
    hash3 = (rounded * 41 + 29) % 983

    # Layer 3: Combine hashes with XOR and rotation
    combined = (hash1 ^ (hash2 << 3) ^ (hash3 >> 2)) % 256

    # Layer 4: Generate multi-byte key using seed
    seed = (rounded * 7 + hash1 + hash2 + hash3) % 65536
    key = []
    state = seed
    for i in range(16):
        state = (state * 1103515245 + 12345) & 0x7fffffff
        key.append(((state >> 16) % 256) & 0xff)

    return bytes(key)

def encrypt_with_kill_count(data, kill_count):
    """
    Multi-layer XOR encryption with position-dependent keys
    """
    key_stream = derive_key_from_kill_count(kill_count)
    result = bytearray(len(data))

    for i in range(len(data)):
        # Use different key bytes for different positions
        key_byte = key_stream[i % len(key_stream)]
        # Add position-dependent transformation
        position_key = ((i * 13 + 7) % 256) & 0xff
        result[i] = (data[i] ^ key_byte ^ position_key) & 0xff

    return bytes(result)

def xor_encrypt(data, key):
    """XOR encryption with a single byte key"""
    return bytes([b ^ key for b in data])

def simple_aes_encrypt(data, key):
    """Simple AES-like transformation (XOR with key stream)"""
    result = bytearray(len(data))
    for i in range(len(data)):
        result[i] = (data[i] ^ key[i % len(key)]) & 0xff
    return bytes(result)

def get_rotation_offset():
    """Get rotation offset from seed string"""
    seed = "PLANTS_VS_ZOMBIES_2025"
    return sum(ord(c) for c in seed) % 26

def rotate_encrypt(text, offset):
    """Rotation cipher encryption"""
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

def substitution_encrypt(text):
    """Substitution cipher encryption"""
    substitution_map = {
        'A': 'Q', 'B': 'W', 'C': 'E', 'D': 'R', 'E': 'T',
        'F': 'Y', 'G': 'U', 'H': 'I', 'I': 'O', 'J': 'P',
        'K': 'A', 'L': 'S', 'M': 'D', 'N': 'F', 'O': 'G',
        'P': 'H', 'Q': 'J', 'R': 'K', 'S': 'L', 'T': 'Z',
        'U': 'X', 'V': 'C', 'W': 'V', 'X': 'B', 'Y': 'N',
        'Z': 'M', '_': '!', '{': '[', '}': ']'
    }
    return ''.join(substitution_map.get(c, c) for c in text)

def generate_encryption(flag, kill_count=260):
    """
    Generate the complete encryption chain
    """
    print(f"Original flag: {flag}")
    print(f"Kill count: {kill_count}")
    print()

    # Step 1: Apply substitution cipher
    substituted = substitution_encrypt(flag)
    print(f"After substitution: {substituted}")

    # Step 2: Apply rotation cipher
    rotation_offset = get_rotation_offset()
    print(f"Rotation offset: {rotation_offset}")
    rotated = rotate_encrypt(substituted, rotation_offset)
    print(f"After rotation: {rotated}")

    # Step 3: Convert to bytes
    intermediate_bytes = rotated.encode('utf-8')
    print(f"As bytes: {intermediate_bytes.hex()}")

    # Step 4: Apply AES-like encryption
    aes_key = bytes([
        0x4a, 0x91, 0xc3, 0x7f, 0x2e, 0xb5, 0x68, 0xd4,
        0x1c, 0x89, 0x3a, 0xf2, 0x5d, 0xa6, 0x71, 0xbe
    ])
    aes_encrypted = simple_aes_encrypt(intermediate_bytes, aes_key)
    print(f"After AES-like: {aes_encrypted.hex()}")

    # Step 5: Split into two chunks
    chunk_size = len(aes_encrypted) // 2
    chunk1 = aes_encrypted[:chunk_size]
    chunk2 = aes_encrypted[chunk_size:]
    print(f"\nChunk 1 ({len(chunk1)} bytes): {chunk1.hex()}")
    print(f"Chunk 2 ({len(chunk2)} bytes): {chunk2.hex()}")

    # Step 6: XOR encrypt each chunk with different keys
    xor_key1 = 0x66
    xor_key2 = 0x77
    encrypted_chunk1 = xor_encrypt(chunk1, xor_key1)
    encrypted_chunk2 = xor_encrypt(chunk2, xor_key2)
    print(f"\nAfter XOR with key1 (0x{xor_key1:02x}): {encrypted_chunk1.hex()}")
    print(f"After XOR with key2 (0x{xor_key2:02x}): {encrypted_chunk2.hex()}")

    # Step 7: Combine chunks
    combined_chunks = encrypted_chunk1 + encrypted_chunk2
    print(f"\nCombined chunks ({len(combined_chunks)} bytes): {combined_chunks.hex()}")

    # Step 8: Encrypt with kill count
    final_encrypted = encrypt_with_kill_count(combined_chunks, kill_count)
    print(f"\nFinal encrypted with kill count ({len(final_encrypted)} bytes):")
    print(final_encrypted.hex())

    # Format as Kotlin byte array
    print("\n" + "="*80)
    print("Kotlin byte array format:")
    print("="*80)
    print("private val killCountEncryptedFlag = byteArrayOf(")

    # Print chunk 1
    print("    // Chunk 1 (13 bytes)")
    for i in range(0, 13, 4):
        line = "    "
        for j in range(4):
            if i + j < 13:
                line += f"0x{final_encrypted[i+j]:02x}.toByte(), "
        print(line.rstrip(", ") + ",")

    # Print chunk 2
    print("    // Chunk 2 (13 bytes)")
    for i in range(13, len(final_encrypted), 4):
        line = "    "
        for j in range(4):
            if i + j < len(final_encrypted):
                line += f"0x{final_encrypted[i+j]:02x}.toByte(), "
        if i + 4 < len(final_encrypted):
            print(line.rstrip(", ") + ",")
        else:
            print(line.rstrip(", "))

    print(")")

    return final_encrypted

def verify_decryption(encrypted_data, kill_count=260):
    """
    Verify that decryption works correctly
    """
    print("\n" + "="*80)
    print("VERIFICATION - Decrypting back:")
    print("="*80)

    # Step 1: Decrypt with kill count
    decrypted = encrypt_with_kill_count(encrypted_data, kill_count)  # XOR is symmetric
    print(f"After kill count decrypt: {decrypted.hex()}")

    # Step 2: Split into chunks
    chunk_size = len(decrypted) // 2
    chunk1 = decrypted[:chunk_size]
    chunk2 = decrypted[chunk_size:]
    print(f"Chunk 1: {chunk1.hex()}")
    print(f"Chunk 2: {chunk2.hex()}")

    # Step 3: XOR decrypt
    xor_key1 = 0x66
    xor_key2 = 0x77
    decrypted_chunk1 = xor_encrypt(chunk1, xor_key1)
    decrypted_chunk2 = xor_encrypt(chunk2, xor_key2)
    print(f"After XOR decrypt chunk1: {decrypted_chunk1.hex()}")
    print(f"After XOR decrypt chunk2: {decrypted_chunk2.hex()}")

    # Step 4: Combine
    combined = decrypted_chunk1 + decrypted_chunk2
    print(f"Combined: {combined.hex()}")

    # Step 5: AES decrypt
    aes_key = bytes([
        0x4a, 0x91, 0xc3, 0x7f, 0x2e, 0xb5, 0x68, 0xd4,
        0x1c, 0x89, 0x3a, 0xf2, 0x5d, 0xa6, 0x71, 0xbe
    ])
    aes_decrypted = simple_aes_encrypt(combined, aes_key)  # XOR is symmetric
    print(f"After AES decrypt: {aes_decrypted.hex()}")

    # Step 6: Convert to string
    intermediate = aes_decrypted.decode('utf-8')
    print(f"As string: {intermediate}")

    # Step 7: Rotation decrypt
    rotation_offset = get_rotation_offset()
    rotated = rotate_encrypt(intermediate, -rotation_offset)  # Negative for decrypt
    print(f"After rotation decrypt: {rotated}")

    # Step 8: Substitution decrypt
    reverse_substitution_map = {
        'Q': 'A', 'W': 'B', 'E': 'C', 'R': 'D', 'T': 'E',
        'Y': 'F', 'U': 'G', 'I': 'H', 'O': 'I', 'P': 'J',
        'A': 'K', 'S': 'L', 'D': 'M', 'F': 'N', 'G': 'O',
        'H': 'P', 'J': 'Q', 'K': 'R', 'L': 'S', 'Z': 'T',
        'X': 'U', 'C': 'V', 'V': 'W', 'B': 'X', 'N': 'Y',
        'M': 'Z', '!': '_', '[': '{', ']': '}'
    }
    final_flag = ''.join(reverse_substitution_map.get(c, c) for c in rotated)
    print(f"Final flag: {final_flag}")

    return final_flag

if __name__ == "__main__":
    # The flag you want to encrypt
    flag = input("Enter the flag to encrypt (e.g., flag{example_flag_here}): ").strip()

    if not flag:
        flag = "flag{test_flag_12345678}"
        print(f"Using default flag: {flag}")

    # Generate encryption
    encrypted = generate_encryption(flag, kill_count=260)

    # Verify it works
    decrypted_flag = verify_decryption(encrypted, kill_count=260)

    print("\n" + "="*80)
    if decrypted_flag == flag:
        print("✓ SUCCESS! Encryption and decryption verified!")
    else:
        print("✗ ERROR! Decryption doesn't match original flag")
        print(f"Expected: {flag}")
        print(f"Got: {decrypted_flag}")
