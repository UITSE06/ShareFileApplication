-- phpMyAdmin SQL Dump
-- version 4.2.11
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Dec 24, 2014 at 10:54 AM
-- Server version: 5.6.21
-- PHP Version: 5.6.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `db_sharefile`
--

-- --------------------------------------------------------

--
-- Table structure for table `file`
--

CREATE TABLE IF NOT EXISTS `file` (
`file_id` int(11) NOT NULL,
  `filename` varchar(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `user_id` int(11) NOT NULL,
  `file_state_id` int(11) NOT NULL,
  `urlfile` varchar(200) NOT NULL,
  `file_role_id` int(11) DEFAULT NULL,
  `dateupload` datetime DEFAULT NULL,
  `size` int(11) DEFAULT NULL,
  `checksum` varchar(500) DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1 COMMENT='Thong tin chi tiet cua mot File upload len server';

--
-- Dumping data for table `file`
--

INSERT INTO `file` (`file_id`, `filename`, `user_id`, `file_state_id`, `urlfile`, `file_role_id`, `dateupload`, `size`, `checksum`) VALUES
(3, 'demo.wmv', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-23 15:26:38', 13421469, 'checkSum001'),
(6, 'nhac giang sinh k loi.mp4', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-23 16:08:54', 152674925, 'checkSum001'),
(7, '[Full] Nhá»¯ng BÃ i ThÃ¡nh Ca Hay Nháº¥t Cá»§a Phan Äinh TÃ¹ng.mp4', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-23 16:09:12', 258569934, 'checkSum001'),
(8, '[phimtorrent.com] Noah  2014 mHD 720p (torrent) - 0.9GB.torrent', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-23 16:21:11', 10022, 'checkSum001'),
(9, '[phimtorrent.com] Noah  2014 mHD 720p (torrent) - 0.9GB.torrent', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-23 16:21:39', 10022, 'checkSum001'),
(10, 'Noah.2014.1080p.BluRay.x264.YIFY.srt', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-23 16:22:25', 91867, 'checkSum001'),
(11, 'Transformers Age of Extinction 2014 720p WEB-DL x264 phimtorrent.com.srt', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-23 16:27:05', 172758, 'checkSum001'),
(12, 'Everyday God.FLV', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-23 16:44:14', 15855505, 'checkSum001'),
(13, 'Larva - Bee.flv', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-23 16:45:10', 6293448, 'checkSum001'),
(14, '2.jpg', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-24 15:59:49', 297320, 'checkSum001'),
(15, '20130819_084744.jpg', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-24 16:28:01', 3790668, 'checkSum001'),
(16, '20130819_090727.jpg', 1, 1, 'C:\\xampp\\tomcat\\SaveFile\\', 1, '2014-12-24 16:36:42', 3604615, 'checkSum001');

-- --------------------------------------------------------

--
-- Table structure for table `file_role`
--

CREATE TABLE IF NOT EXISTS `file_role` (
`file_role_id` int(11) NOT NULL,
  `role_name` varchar(45) DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `file_role`
--

INSERT INTO `file_role` (`file_role_id`, `role_name`) VALUES
(1, 'private');

-- --------------------------------------------------------

--
-- Table structure for table `file_server`
--

CREATE TABLE IF NOT EXISTS `file_server` (
`file_server_id` int(11) NOT NULL,
  `file_id` int(11) NOT NULL,
  `server_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `file_state`
--

CREATE TABLE IF NOT EXISTS `file_state` (
`file_state_id` int(11) NOT NULL,
  `state_name` varchar(45) DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `file_state`
--

INSERT INTO `file_state` (`file_state_id`, `state_name`) VALUES
(1, 'uploading');

-- --------------------------------------------------------

--
-- Table structure for table `server`
--

CREATE TABLE IF NOT EXISTS `server` (
`server_id` int(11) NOT NULL,
  `IP_server` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE IF NOT EXISTS `user` (
`user_id` int(11) NOT NULL,
  `username` varchar(30) NOT NULL,
  `password` varchar(30) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `email` varchar(45) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COMMENT='Chua thong tin cua user, gom cac quyen: Quan tri, user';

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`user_id`, `username`, `password`, `name`, `email`) VALUES
(1, 'quanta', 'anhquan', 'Tran Anh Quan', 'johnanhquan@gmail.com');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `file`
--
ALTER TABLE `file`
 ADD PRIMARY KEY (`file_id`), ADD KEY `fk_stateid_idx` (`file_state_id`), ADD KEY `user_id_fk_idx` (`user_id`), ADD KEY `role_file_fk_idx` (`file_role_id`);

--
-- Indexes for table `file_role`
--
ALTER TABLE `file_role`
 ADD PRIMARY KEY (`file_role_id`);

--
-- Indexes for table `file_server`
--
ALTER TABLE `file_server`
 ADD PRIMARY KEY (`file_server_id`), ADD KEY `file_detail_fk_idx` (`file_id`), ADD KEY `server_detail_fk_idx` (`server_id`);

--
-- Indexes for table `file_state`
--
ALTER TABLE `file_state`
 ADD PRIMARY KEY (`file_state_id`);

--
-- Indexes for table `server`
--
ALTER TABLE `server`
 ADD PRIMARY KEY (`server_id`), ADD UNIQUE KEY `IP_server_UNIQUE` (`IP_server`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
 ADD PRIMARY KEY (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `file`
--
ALTER TABLE `file`
MODIFY `file_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=17;
--
-- AUTO_INCREMENT for table `file_role`
--
ALTER TABLE `file_role`
MODIFY `file_role_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
--
-- AUTO_INCREMENT for table `file_server`
--
ALTER TABLE `file_server`
MODIFY `file_server_id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `file_state`
--
ALTER TABLE `file_state`
MODIFY `file_state_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
--
-- AUTO_INCREMENT for table `server`
--
ALTER TABLE `server`
MODIFY `server_id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=2;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `file`
--
ALTER TABLE `file`
ADD CONSTRAINT `role_file_fk` FOREIGN KEY (`file_role_id`) REFERENCES `file_role` (`file_role_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
ADD CONSTRAINT `state_file_fk` FOREIGN KEY (`file_state_id`) REFERENCES `file_state` (`file_state_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
ADD CONSTRAINT `user_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints for table `file_server`
--
ALTER TABLE `file_server`
ADD CONSTRAINT `file_detail_fk` FOREIGN KEY (`file_id`) REFERENCES `file` (`file_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
ADD CONSTRAINT `server_detail_fk` FOREIGN KEY (`server_id`) REFERENCES `server` (`server_id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
