-- phpMyAdmin SQL Dump
-- version 4.2.11
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Jan 26, 2015 at 08:03 AM
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
  `filename` varchar(500) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `file_title` varchar(200) NOT NULL,
  `username` varchar(100) NOT NULL,
  `file_state_id` int(11) NOT NULL,
  `urlfile` varchar(200) NOT NULL,
  `file_role_id` int(11) DEFAULT NULL,
  `dateupload` datetime DEFAULT NULL,
  `size` int(11) DEFAULT NULL,
  `checksum` varchar(500) DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1 COMMENT='Thong tin chi tiet cua mot File upload len server';

--
-- Dumping data for table `file`
--

-- --------------------------------------------------------

--
-- Table structure for table `file_role`
--

CREATE TABLE IF NOT EXISTS `file_role` (
`file_role_id` int(11) NOT NULL,
  `role_name` varchar(45) DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `file_role`
--

INSERT INTO `file_role` (`file_role_id`, `role_name`) VALUES
(1, 'private'),
(2, 'shared'),
(3, 'public');

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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `file_state`
--

INSERT INTO `file_state` (`file_state_id`, `state_name`) VALUES
(1, 'uploading'),
(2, 'uploaded'),
(3, 'deleted'),
(4, 'uploadFail'),
(5, 'recycled');

-- --------------------------------------------------------

--
-- Table structure for table `log_transfer`
--

CREATE TABLE IF NOT EXISTS `log_transfer` (
`log_id` int(11) NOT NULL,
  `source_server` varchar(30) NOT NULL,
  `file_title` varchar(200) NOT NULL,
  `server_ip` varchar(30) NOT NULL,
  `action` int(11) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `log_transfer`
--

-- --------------------------------------------------------

--
-- Table structure for table `server`
--

CREATE TABLE IF NOT EXISTS `server` (
`server_id` int(11) NOT NULL,
  `IP_server` varchar(45) DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `server`
--

INSERT INTO `server` (`server_id`, `IP_server`) VALUES
(1, '104.155.199.62'),
(4, '104.155.204.35'),
(3, '104.155.210.44'),
(2, '107.167.180.164');

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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1 COMMENT='Chua thong tin cua user, gom cac quyen: Quan tri, user';

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`user_id`, `username`, `password`, `name`, `email`) VALUES
(1, 'quanta', 'anhquan', 'Tran Anh Quan', 'johnanhquan@gmail.com'),
(2, 'anhquan', '123456', 'anh quan tran', 'abc@def.com'),
(3, 'heartsmile', '123456', 'ai do', 'abc@def.com');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `file`
--
ALTER TABLE `file`
 ADD PRIMARY KEY (`file_id`), ADD KEY `fk_stateid_idx` (`file_state_id`), ADD KEY `role_file_fk_idx` (`file_role_id`);

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
-- Indexes for table `log_transfer`
--
ALTER TABLE `log_transfer`
 ADD PRIMARY KEY (`log_id`);

--
-- Indexes for table `server`
--
ALTER TABLE `server`
 ADD PRIMARY KEY (`server_id`), ADD UNIQUE KEY `IP_server_UNIQUE` (`IP_server`);

--
-- Indexes for table `user`
--
ALTER TABLE `user`
 ADD PRIMARY KEY (`user_id`), ADD UNIQUE KEY `username` (`username`), ADD UNIQUE KEY `username_2` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `file`
--
ALTER TABLE `file`
MODIFY `file_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=1;
--
-- AUTO_INCREMENT for table `file_role`
--
ALTER TABLE `file_role`
MODIFY `file_role_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=4;
--
-- AUTO_INCREMENT for table `file_server`
--
ALTER TABLE `file_server`
MODIFY `file_server_id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `file_state`
--
ALTER TABLE `file_state`
MODIFY `file_state_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=6;
--
-- AUTO_INCREMENT for table `log_transfer`
--
ALTER TABLE `log_transfer`
MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=1;
--
-- AUTO_INCREMENT for table `server`
--
ALTER TABLE `server`
MODIFY `server_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=5;
--
-- AUTO_INCREMENT for table `user`
--
ALTER TABLE `user`
MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=4;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `file`
--
ALTER TABLE `file`
ADD CONSTRAINT `filestate_fk` FOREIGN KEY (`file_state_id`) REFERENCES `file_state` (`file_state_id`),
ADD CONSTRAINT `rolefile_fk` FOREIGN KEY (`file_role_id`) REFERENCES `file_role` (`file_role_id`);

--
-- Constraints for table `file_server`
--
ALTER TABLE `file_server`
ADD CONSTRAINT `fileidserver_fk` FOREIGN KEY (`file_id`) REFERENCES `file` (`file_id`),
ADD CONSTRAINT `serverid_fk` FOREIGN KEY (`server_id`) REFERENCES `server` (`server_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
