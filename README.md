# X1-BTC-PSBT-Firmware

**Bitcoin-only PSBT firmware for the Cypherock X1 hardware wallet**  
[![License: GPL-3.0](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

This project provides a minimalist, Bitcoin-only firmware image for the Cypherock X1 device. It implements:
- BIP-32/39/44 hierarchical key derivation  
- BIP-174 PSBT signing flows compatible with Specter, Sparrow, Wasabi  
- Embedded C/C++ runtime with hardware-specific I/O and secure-element interfacing  
