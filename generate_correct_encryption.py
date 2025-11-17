#!/usr/bin/env python3
"""
Generate correct encryption for kill count flag.
Only kill counts that round to 260 (255-264) should decrypt correctly.
"""

def round_to_nearest_10(kill_count):
    """Round kill count to nearest 10"""
    return ((kill_count + 5) // 10) * 10

def derive_key_from_kill_count(kill_count):
    """Derive encryption key from kill count"""
    rounded = round_to_nearest_10(kill_count)

    # Only use correct value if rounded equals 260
    if rounded == 260:
        effective_count = 260
    else:
        effective_count = rounded * 3 + 17

    # Formula: (effectiveCount * 7 + 13) % 256
    return ((effective_count * 7 + 13) % 256) & 0xFF

def xor_encrypt(data, key):
    """XOR encrypt data with key"""
    return bytes([b ^ key for b in data])

# The flag we want to encrypt
flag = b"flag{y0u_k1ll3d_260_z0mb}"

# Get the correct key for kill count 260
correct_key = derive_key_from_kill_count(260)
print(f"Correct key for 260 kills: 0x{correct_key:02x} ({correct_key})")

# Encrypt the flag
encrypted = xor_encrypt(flag, correct_key)

# Print as Kotlin byte array
print("\nKotlin byte array:")
print("private val killCountEncryptedFlag = byteArrayOf(")
for i in range(0, len(encrypted), 4):
    chunk = encrypted[i:i+4]
    hex_values = ", ".join([f"0x{b:02x}.toByte()" for b in chunk])
    print(f"    {hex_values},")
print(")")

# Test decryption with different kill counts
print("\n=== Testing decryption ===")
test_counts = [250, 255, 258, 260, 262, 264, 270, 259, 269]
for count in test_counts:
    key = derive_key_from_kill_count(count)
    decrypted = xor_encrypt(encrypted, key)
    rounded = round_to_nearest_10(count)
    try:
        result = decrypted.decode('utf-8')
        valid = result.startswith('flag{') and result.endswith('}')
        status = "✓ VALID" if valid else "✗ INVALID"
    except:
        result = "<binary garbage>"
        status = "✗ INVALID"
    print(f"Count {count:3d} (rounds to {rounded:3d}): key=0x{key:02x} -> {result[:30]} {status}")
