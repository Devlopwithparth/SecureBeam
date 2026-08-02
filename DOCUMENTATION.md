# SecureBeam - Technical Documentation & System Specifications

**SecureBeam** is a zero-trust, air-gapped Android application designed for ultra-secure offline data exchange between devices. It completely eliminates network dependencies (Wi-Fi, Bluetooth, NFC, Mobile Data, Internet) by converting encrypted binary files into high-speed animated QR code streams scanned via CameraX.

---

## 1. System Architecture & UML Overview

### 1.1 High-Level Architecture Diagram
```mermaid
graph TD
    subgraph UI_Layer ["UI Layer (Jetpack Compose Material 3 Dark Theme)"]
        Auth["AuthScreen (PIN / Biometrics)"]
        Dash["DashboardScreen (Analytics & Storage)"]
        SenderUI["SenderScreen (File Pick & Live QR Stream)"]
        ReceiverUI["ReceiverScreen (CameraX Live Scanner)"]
        SecurityUI["SecurityAuditScreen (Audit Logs & DoD Shredder)"]
        SettingsUI["SettingsScreen (FPS & Density Tuner)"]
    end

    subgraph Domain_Layer ["Domain & Transfer Engine"]
        Crypto["CryptoEngine (AES-256-GCM / RSA-2048 / SHA-256)"]
        Packager["FilePackager (Deflater Compression & Frame Chunking)"]
        Reassembler["PacketReassembler (Buffer Matrix & Inflater)"]
        QRGen["QRGenerator (ZXing Matrix Renderer)"]
        CamAnalyzer["CameraFrameAnalyzer (CameraX ImageAnalysis)"]
    end

    subgraph Data_Layer ["Data & Persistence Layer"]
        Repo["SecureBeamRepository"]
        RoomDB[("Room Database (Transfers, Audit Logs, Devices)")]
        Shredder["SecureShredder (3-Pass DoD 5220.22-M Wipe)"]
    end

    Auth --> Dash
    Dash --> SenderUI
    Dash --> ReceiverUI
    Dash --> SecurityUI
    Dash --> SettingsUI

    SenderUI --> Packager
    Packager --> Crypto
    Packager --> QRGen

    ReceiverUI --> CamAnalyzer
    CamAnalyzer --> Reassembler
    Reassembler --> Crypto
    Reassembler --> Repo

    SecurityUI --> Shredder
    SecurityUI --> Repo
    Repo --> RoomDB
```

---

## 2. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    TRANSFER_RECORD {
        int id PK
        string fileName
        long fileSize
        string fileType
        string direction
        string status
        float speedKbps
        float durationSeconds
        long timestamp
        string fileHash
        string certificateId FK
    }

    AUDIT_EVENT {
        int id PK
        long timestamp
        string eventType
        string severity
        string details
        string deviceId FK
    }

    TRUSTED_DEVICE {
        string deviceId PK
        string deviceName
        string publicKeyPem
        int trustScore
        int totalTransfers
        long lastSeen
    }

    SECURITY_CERTIFICATE {
        string certificateId PK
        long transferRecordId FK
        string fileName
        long fileSize
        string sha256Hash
        string digitalSignature
        long issuedAt
    }

    TRUSTED_DEVICE ||--o{ AUDIT_EVENT : "logs"
    TRANSFER_RECORD ||--|| SECURITY_CERTIFICATE : "issues"
```

---

## 3. Data Flow Diagrams (DFD)

### 3.1 DFD Level 0 (Context Diagram)
```mermaid
graph LR
    SenderDevice["Sender Device"] -->|"Animated QR Code Stream"| SecureBeamSystem["SecureBeam Optical Engine"]
    SecureBeamSystem -->|"Decrypted File & Audit Logs"| ReceiverDevice["Receiver Device"]
```

### 3.2 DFD Level 1 (Process Breakdown)
```mermaid
graph TD
    User["User"] -->|Selects File| P1["Process 1: Compression & AES-256 Encryption"]
    P1 -->|Encrypted Payload| P2["Process 2: Frame Chunking & CRC32 Signing"]
    P2 -->|Frame Packets| P3["Process 3: Animated QR Stream Rendering"]
    P3 -->|Optical Stream| P4["Process 4: CameraX Frame Capture"]
    P4 -->|Decoded QR String| P5["Process 5: Packet Reassembly & Hash Verification"]
    P5 -->|Verified Payload| P6["Process 6: Decryption & File Save"]
    P6 -->|Transfer Record| DB[("Room Database")]
```

### 3.3 DFD Level 2 (Detailed Optical Transfer Process)
```mermaid
graph TD
    subgraph Sender_Side ["Sender Process"]
        F1["Raw File Bytes"] --> C1["Deflater Compression"]
        C1 --> E1["AES-256-GCM Encrypt with Session IV"]
        E1 --> K1["Split into 384-byte Chunks"]
        K1 --> S1["Calculate CRC32 Integrity Tag"]
        S1 --> Q1["ZXing QR Bitmap Generation"]
    end

    subgraph Receiver_Side ["Receiver Process"]
        Q1 -->|Camera Sensor| V1["CameraX ImageAnalysis Callback"]
        V1 --> D1["ZXing Barcode Decoder"]
        D1 --> R1["Packet Buffer Index Validator"]
        R1 --> H1["SHA-256 Checksum Verification"]
        H1 --> AES1["AES-256 Decryption"]
        AES1 --> Z1["Inflater Decompression"]
        Z1 --> W1["Write File to Storage"]
    end
```

---

## 4. Use Case Diagram

```mermaid
graph LR
    User(("Mobile User / Security Officer"))

    subgraph SecureBeam ["SecureBeam Air-Gapped Platform"]
        UC1["Authenticate via PIN / Biometrics"]
        UC2["Select File & Initiate Sender Stream"]
        UC3["Tune Optical Speed (5-30 FPS) & QR Density"]
        UC4["Scan Animated QR Code via CameraX"]
        UC5["Monitor Packet Reassembly Progress"]
        UC6["Inspect Cyber Security Audit Logs"]
        UC7["Perform DoD 5220.22-M File Shredding"]
        UC8["Export Transfer Verification Certificates"]
    end

    User --> UC1
    User --> UC2
    User --> UC3
    User --> UC4
    User --> UC5
    User --> UC6
    User --> UC7
    User --> UC8
```

---

## 5. Sequence Diagram

### 5.1 Sender & Receiver Sequence Flow
```mermaid
sequenceDiagram
    autonumber
    actor Sender as Sender User
    participant SenderApp as Sender ViewModel
    participant Crypto as Crypto Engine
    participant QRStream as Animated QR Streamer
    participant Camera as CameraX Scanner
    participant ReceiverApp as Receiver ViewModel
    actor Receiver as Receiver User

    Sender->>SenderApp: Select File (e.g. document.pdf)
    SenderApp->>Crypto: Compress (Deflater) & Encrypt (AES-256-GCM)
    Crypto-->>SenderApp: Encrypted Chunks & Session IV
    SenderApp->>QRStream: Generate 384-byte Frame Packets with CRC32
    QRStream->>Sender: Display High-Speed QR Animation (15-30 FPS)

    Receiver->>Camera: Launch CameraX Viewfinder
    loop Frame Capture Loop
        Camera->>ReceiverApp: Capture Frame & Decode QR String
        ReceiverApp->>ReceiverApp: Verify CRC32 & Add to Buffer Matrix
        ReceiverApp-->>Receiver: Update Progress Bar (% Complete)
    end

    ReceiverApp->>Crypto: Reassemble Chunks, Verify SHA-256 & Decrypt AES-256
    Crypto-->>ReceiverApp: Original File Bytes
    ReceiverApp->>Receiver: Save File & Issue Security Certificate
```

---

## 6. Activity Diagram

```mermaid
graph TD
    Start([Start Transfer]) --> AuthCheck{Is Authenticated?}
    AuthCheck -- No --> PromptAuth[Prompt Biometrics / PIN]
    PromptAuth --> AuthCheck
    AuthCheck -- Yes --> SelectRole{Select Action}

    SelectRole -- Sender --> PickFile[Select Target File]
    PickFile --> CompressFile[Deflate Compress File]
    CompressFile --> EncryptFile[Encrypt via AES-256-GCM]
    EncryptFile --> ChunkPackets[Split into Frame Packets + CRC32]
    ChunkPackets --> LoopQR[Loop Animated QR Stream at Target FPS]

    SelectRole -- Receiver --> OpenCam[Launch CameraX Scanner]
    OpenCam --> ScanFrame[Scan QR Frame]
    ScanFrame --> CheckCRC{Valid CRC32?}
    CheckCRC -- No --> DropFrame[Discard Corrupted Frame]
    DropFrame --> ScanFrame
    CheckCRC -- Yes --> BufferFrame[Store in Buffer Matrix]
    BufferFrame --> AllReceived{All Packets Captured?}
    AllReceived -- No --> ScanFrame
    AllReceived -- Yes --> VerifyHash{SHA-256 Hash Match?}
    VerifyHash -- No --> FlagError[Flag Integrity Tamper Alert]
    VerifyHash -- Yes --> DecryptSave[Decrypt AES-256 & Save File]
    DecryptSave --> AuditLog[Write Audit Log & Transfer Certificate]
    AuditLog --> End([End Operation])
```

---

## 7. Optical Frame Protocol Specification

### Packet Payload Structure
Each frame is encoded as a pipe-separated string with prefix `SECBEAM`:
`SECBEAM|SessionID|PacketIndex|TotalPackets|FileName|FileSize|FileHash|IV|Key|PayloadChunk|CRC32`

| Field | Type | Description |
| :--- | :--- | :--- |
| `Header` | String | Fixed identifier (`SECBEAM`) |
| `SessionID` | String | 8-character random session UUID |
| `PacketIndex` | Integer | 1-based index of current frame |
| `TotalPackets` | Integer | Total number of frames in transmission |
| `FileName` | String | Original file name |
| `FileSize` | Long | Size of original file in bytes |
| `FileHash` | String | SHA-256 hash of original file |
| `IV` | String | Base64-encoded 12-byte AES-GCM IV |
| `Key` | String | Base64-encoded 256-bit AES Session Key |
| `PayloadChunk` | String | Base64-encoded encrypted chunk payload |
| `CRC32` | Long | CRC-32 checksum of previous fields |

---

## 8. Database Schema Specifications

### `transfer_records` Table
| Column Name | Data Type | Constraint | Description |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | Primary Key, Auto-increment | Unique record ID |
| `fileName` | TEXT | NOT NULL | File name transferred |
| `fileSize` | INTEGER | NOT NULL | Size in bytes |
| `fileType` | TEXT | NOT NULL | File extension category |
| `direction` | TEXT | NOT NULL | `SENDER` or `RECEIVER` |
| `status` | TEXT | NOT NULL | `COMPLETED`, `FAILED`, `INTERRUPTED` |
| `speedKbps` | REAL | NOT NULL | Speed in Kilobits/sec |
| `durationSeconds`| REAL | NOT NULL | Transfer elapsed time |
| `timestamp` | INTEGER | NOT NULL | Unix timestamp |
| `fileHash` | TEXT | NOT NULL | SHA-256 hash digest |
| `certificateId` | TEXT | NOT NULL | Unique transfer certificate ID |

---

## 9. Future Scope & Enhancement Roadmap

1. **Optical Reed-Solomon Error Correction**: Adding forward error correction (FEC) parity frames to recover missing QR packets without retransmission.
2. **Multi-Device Broadcast Transfer**: Streaming high-speed optical QR codes to multiple receiving devices simultaneously.
3. **PC Companion App**: Windows / Linux / macOS companion app for cross-platform air-gapped transfers.
4. **Hardware Key Integration**: YubiKey / HSM support for hardware-backed RSA digital signatures.
