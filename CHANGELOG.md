# Changelog – BonePipe

All notable changes to this project are documented here.

The format roughly follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) and the project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [3.0.12] – 2025-10-24

### Fixed
- Saved adapter configuration no longer resets after world reload.
- Resolved a null pointer crash during world load when adapters reconnect.

### Improved
- Server/client data sync now keeps GUI state and transfer metrics aligned.
- Removed legacy integrations, recipes, and documentation so the repository matches the streamlined 3.x feature set.

## [3.0.0] – 2024-10-22

### Changed
- Refocused the mod on a single Wireless Adapter block with channel-based transfers.
- Removed the Network Controller, upgrade cards, and side inventory slots for a streamlined setup.

### Added
- Frequency-based networking for items, fluids, energy, and Mekanism gas.
- Basic whitelist filtering per channel plus optional chunk-loading support.
- Compact adapter GUI with frequency input and per-channel mode toggles.
