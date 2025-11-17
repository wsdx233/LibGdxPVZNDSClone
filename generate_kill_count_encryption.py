#!/usr/bin/env python3
"""
Generate encrypted flag data that requires zombie kill count = 260 to decrypt
"""

def xor_encrypt(data: bytes, key: int) -> bytes:
    """XOR encryption with a single byte key"""
    return bytes([b ^ key for b in data])

def derive_key_from_kill_count(kill_count: int) -> int:
    """Derive encryption key from kill count"""
    # Use a formula that only works correctly with 260
    # Formula: (kill_count * 7 + 13) % 256
    return (kill_count * 7 + 13) % 256

def format_byte_array(data: bytes, name: str) -> str:
    """Format byte array for Kotlin code"""
    hex_values = [f"0x{b:02x}.toByte()" for b in data]

    # Format in rows of 4 values
    rows = []
    for i in range(0, len(hex_values), 4):
        row = ", ".join(hex_values[i:i+4])
        rows.append(f"        {row}")

    result = f"    private val {name} = byteArrayOf(\n"
    result += ",\n".join(rows)
    result += "\n    )"

    return result

def main():
    # The flag we want to encrypt
    flag = "flag{y0u_k1ll3d_th3m_4ll}"

    print("=" * 60)
    print("Flag Encryption Generator")
    print("=" * 60)
    print(f"\nOriginal flag: {flag}")
    print(f"Flag length: {len(flag)}")

    # Correct kill count
    correct_kill_count = 260
    correct_key = derive_key_from_kill_count(correct_kill_count)

    print(f"\nCorrect kill count: {correct_kill_count}")
    print(f"Derived key: {correct_key} (0x{correct_key:02x})")

    # Encrypt the flag
    flag_bytes = flag.encode('utf-8')
    encrypted = xor_encrypt(flag_bytes, correct_key)

    print(f"\nEncrypted bytes ({len(encrypted)} bytes):")
    print(" ".join(f"{b:02x}" for b in encrypted))

    # Generate Kotlin code
    print("\n" + "=" * 60)
    print("Kotlin Code (paste into FlagScreen.kt):")
    print("=" * 60)
    print()
    print(format_byte_array(encrypted, "killCountEncryptedFlag"))

    # Test with wrong kill counts
    print("\n" + "=" * 60)
    print("Verification - Testing with different kill counts:")
    print("=" * 60)

    test_counts = [0, 100, 200, 259, 260, 261, 300]
    for count in test_counts:
        key = derive_key_from_kill_count(count)
        decrypted = xor_encrypt(encrypted, key)
        try:
            result = decrypted.decode('utf-8', errors='replace')
            # Check if it's valid ASCII
            is_valid = all(32 <= ord(c) <= 126 for c in result)
            status = "✓ VALID" if result == flag else "✗ INVALID"
            print(f"Kill count {count:3d} -> key 0x{key:02x} -> {status}: {result[:30]}")
        except:
            print(f"Kill count {count:3d} -> key 0x{key:02x} -> ✗ INVALID: [decode error]")

    print("\n" + "=" * 60)
    print("Key derivation formula:")
    print("=" * 60)
    print("key = (killCount * 7 + 13) % 256")
    print(f"\nFor killCount = 260:")
    print(f"  key = (260 * 7 + 13) % 256")
    print(f"  key = (1820 + 13) % 256")
    print(f"  key = 1833 % 256")
    print(f"  key = {correct_key}")

if __name__ == "__main__":
    main()
