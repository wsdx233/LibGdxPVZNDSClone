#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Final verification that only kill counts 255-264 (rounding to 260) can decrypt the flag.
"""

def derive_key_from_kill_count(kill_count):
    """Derive encryption key - matches Kotlin logic exactly"""
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
        key.append(((state >> 16) % 256) & 0xFF)

    return bytes(key)

def decrypt_with_kill_count(data, kill_count):
    """Decrypt data using kill count - matches Kotlin logic"""
    key_stream = derive_key_from_kill_count(kill_count)
    result = bytearray(len(data))

    for i in range(len(data)):
        # Use different key bytes for different positions
        key_byte = key_stream[i % len(key_stream)]
        # Add position-dependent transformation
        position_key = ((i * 13 + 7) % 256) & 0xFF
        result[i] = (data[i] ^ key_byte ^ position_key) & 0xFF

    return bytes(result)

# The encrypted flag from FlagScreen.kt
encrypted_flag = bytes([
    0x30, 0x05, 0x4b, 0x19,
    0x77, 0x22, 0xe8, 0xec,
    0x38, 0x3f, 0xf6, 0xaa,
    0xf0, 0x93, 0x5e, 0x5f,
    0xb4, 0xaf, 0xca, 0xf1,
    0x46, 0x3b, 0xc5, 0xab,
    0x4a
])

print("=" * 80)
print("Final Verification - Kill Count Encryption")
print("=" * 80)
print("\nExpected behavior:")
print("  PASS: Kill counts 255-264 (round to 260) should decrypt to valid flag")
print("  PASS: All other kill counts should produce garbage\n")

# Comprehensive test cases
test_cases = [
    # Edge cases and common values
    (0, False, "Zero"),
    (1, False, "Main.kt test value"),
    (10, False, "Multiple of 10"),
    (100, False, "Round number"),

    # Values near 260 but outside range
    (245, False, "Rounds to 250"),
    (250, False, "Rounds to 250"),
    (254, False, "Just below range - rounds to 250"),

    # Valid range (255-264)
    (255, True, "Lower bound - rounds to 260"),
    (256, True, "Rounds to 260"),
    (257, True, "Rounds to 260"),
    (258, True, "Rounds to 260"),
    (259, True, "Rounds to 260 (old vulnerable value)"),
    (260, True, "Exact target - rounds to 260"),
    (261, True, "Rounds to 260"),
    (262, True, "Rounds to 260"),
    (263, True, "Rounds to 260"),
    (264, True, "Upper bound - rounds to 260"),

    # Values above range
    (265, False, "Just above range - rounds to 270"),
    (269, False, "Rounds to 270 (old vulnerable value)"),
    (270, False, "Rounds to 270"),
    (279, False, "Rounds to 280 (old vulnerable value)"),
    (300, False, "High value"),
]

print(f"{'Count':<7} {'Rounds':<8} {'Result':<30} {'Status':<6} {'Note'}")
print("-" * 90)

pass_count = 0
fail_count = 0

for count, should_work, note in test_cases:
    rounded = ((count + 5) // 10) * 10
    decrypted = decrypt_with_kill_count(encrypted_flag, count)

    try:
        result = decrypted.decode('utf-8')
        is_valid = result.startswith('flag{') and result.endswith('}')
        result_str = result if is_valid else "<garbage>"
    except:
        is_valid = False
        result_str = "<decode error>"

    # Check if result matches expectation
    test_passed = (is_valid == should_work)

    if test_passed:
        status = "PASS"
        pass_count += 1
        symbol = "OK" if should_work else "X"
    else:
        status = "FAIL"
        fail_count += 1
        symbol = "!!"

    print(f"{count:<7} {rounded:<8} {result_str:<30} {symbol} {status:<4} {note}")

print("-" * 90)
print(f"\nTest Results: {pass_count}/{len(test_cases)} passed, {fail_count} failed")

if fail_count == 0:
    print("\n SUCCESS! All tests passed!")
    print("  - Only kill counts 255-264 can decrypt the flag")
    print("  - Old vulnerability (10*n-1) is fixed")
    print("  - Complex multi-layer encryption is working correctly")
else:
    print(f"\n FAILURE! {fail_count} test(s) failed.")
    print("  Please review the encryption implementation.")
