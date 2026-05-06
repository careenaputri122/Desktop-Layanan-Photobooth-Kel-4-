CREATE DATABASE photobooth_db;

USE photobooth_db;

<<<<<<< Updated upstream
CREATE TABLE users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    namaDepan    VARCHAR(100) NOT NULL,
    namaBelakang VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL DEFAULT 'user'
);
=======
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `photobooth_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `bookings`
--

CREATE TABLE `bookings` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `paket_id` int NOT NULL,
  `tanggal` date NOT NULL,
  `jam_mulai` varchar(50) NOT NULL,
  `lokasi` varchar(255) NOT NULL,
  `nama_pemesan` varchar(200) NOT NULL,
  `email` varchar(150) NOT NULL,
  `phone` varchar(30) NOT NULL,
  `catatan` text,
  `status` varchar(30) NOT NULL DEFAULT 'Menunggu Konfirmasi',
  `nomor_pesanan` varchar(30) NOT NULL,
  `total_harga` int NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`id`, `user_id`, `paket_id`, `tanggal`, `jam_mulai`, `lokasi`, `nama_pemesan`, `email`, `phone`, `catatan`, `status`, `nomor_pesanan`, `total_harga`, `created_at`) VALUES
(1, 2, 1, '2026-04-30', 'Pagi (08.00 – 12.00)', 'layo inilah', 'budi jona', 'jona123@gmail.com', '085361113051', 'cepet kak, nak bepoto', 'Menunggu Konfirmasi', 'FTM-2026-784', 1275000, '2026-04-29 15:44:49');

-- --------------------------------------------------------

--
-- Table structure for table `paket`
--

CREATE TABLE `paket` (
  `id` int NOT NULL,
  `nama` varchar(100) NOT NULL,
  `harga` int NOT NULL,
  `tipe` varchar(50) NOT NULL,
  `keterangan` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `paket`
--

INSERT INTO `paket` (`id`, `nama`, `harga`, `tipe`, `keterangan`) VALUES
(1, 'Paket Starter', 1275000, 'Cetak', 'Operator profesional\nBackdrop sesuai paket\nProps photobooth\nCetak foto sesuai paket\nDigital file semua foto\nSetup sebelum acara'),
(2, 'Paket Silver', 1700000, 'Cetak', 'Operator profesional\nBackdrop sesuai paket\nProps photobooth\nCetak foto sesuai paket\nDigital file semua foto\nSetup sebelum acara'),
(3, 'Paket Gold', 2337500, 'Cetak', 'Operator profesional\nBackdrop sesuai paket\nProps photobooth\nCetak foto sesuai paket\nDigital file semua foto\nSetup sebelum acara'),
(4, 'Paket Digital', 1062500, 'Tanpa Cetak', '3 jam operasional\nBackdrop 2 pilihan\nProps standar 20 pcs\nDigital file semua foto\nShare via QR Code\n1 operator profesional');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int NOT NULL,
  `namaDepan` varchar(100) NOT NULL,
  `namaBelakang` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'user'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO users (id, namaDepan, namaBelakang, email, password, role) VALUES
(1, 'Admin', 'Utama', 'admin@aksaf.id', 'admin123', 'admin');

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `namaDepan`, `namaBelakang`, `email`, `password`, `role`) VALUES
(2, 'budi', 'jona', 'jona123@gmail.com', '97bdc896ca0ce9549f8bd92f3288e7ee', 'user');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nomor_pesanan` (`nomor_pesanan`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `paket_id` (`paket_id`);

--
-- Indexes for table `paket`
--
ALTER TABLE `paket`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `paket`
--
ALTER TABLE `paket`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`paket_id`) REFERENCES `paket` (`id`) ON DELETE RESTRICT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
>>>>>>> Stashed changes
