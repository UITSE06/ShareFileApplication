-- phpMyAdmin SQL Dump
-- version 4.0.10deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jan 18, 2015 at 04:46 PM
-- Server version: 5.5.40-0ubuntu0.14.04.1
-- PHP Version: 5.5.9-1ubuntu4.5

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
  `file_id` int(11) NOT NULL AUTO_INCREMENT,
  `filename` varchar(500) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `file_title` varchar(200) NOT NULL,
  `username` varchar(100) NOT NULL,
  `file_state_id` int(11) NOT NULL,
  `urlfile` varchar(200) NOT NULL,
  `file_role_id` int(11) DEFAULT NULL,
  `dateupload` datetime DEFAULT NULL,
  `size` int(11) DEFAULT NULL,
  `checksum` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`file_id`),
  KEY `fk_stateid_idx` (`file_state_id`),
  KEY `role_file_fk_idx` (`file_role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Thong tin chi tiet cua mot File upload len server' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `file_role`
--

CREATE TABLE IF NOT EXISTS `file_role` (
  `file_role_id` int(11) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`file_role_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

-- --------------------------------------------------------

--
-- Table structure for table `file_server`
--

CREATE TABLE IF NOT EXISTS `file_server` (
  `file_server_id` int(11) NOT NULL AUTO_INCREMENT,
  `file_id` int(11) NOT NULL,
  `server_id` int(11) NOT NULL,
  PRIMARY KEY (`file_server_id`),
  KEY `file_detail_fk_idx` (`file_id`),
  KEY `server_detail_fk_idx` (`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `file_state`
--

CREATE TABLE IF NOT EXISTS `file_state` (
  `file_state_id` int(11) NOT NULL AUTO_INCREMENT,
  `state_name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`file_state_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=6 ;

-- --------------------------------------------------------

--
-- Table structure for table `server`
--

CREATE TABLE IF NOT EXISTS `server` (
  `server_id` int(11) NOT NULL AUTO_INCREMENT,
  `IP_server` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`server_id`),
  UNIQUE KEY `IP_server_UNIQUE` (`IP_server`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Dumping data for table `server`
--

INSERT INTO `server` (`server_id`, `IP_server`) VALUES
(1, '104.155.199.62'),
(3, '104.155.210.44'),
(2, '107.167.180.164'),
(4, '130.211.245.75');

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) NOT NULL,
  `password` varchar(30) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `email` varchar(45) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `username_2` (`username`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Chua thong tin cua user, gom cac quyen: Quan tri, user' AUTO_INCREMENT=4 ;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`user_id`, `username`, `password`, `name`, `email`) VALUES
(1, 'quanta', 'anhquan', 'Tran Anh Quan', 'johnanhquan@gmail.com'),
(2, 'anhquan', '123456', 'anh quan tran', 'abc@def.com'),
(3, 'heartsmile', '123456', 'ai do', 'abc@def.com');

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
  ADD CONSTRAINT `serverid_fk` FOREIGN KEY (`server_id`) REFERENCES `server` (`server_id`),
  ADD CONSTRAINT `fileidserver_fk` FOREIGN KEY (`file_id`) REFERENCES `file` (`file_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

INSERT INTO `file_role` (`file_role_id`, `role_name`) VALUES
(1, 'private'),
(2, 'shared'),
(3, 'public');

INSERT INTO `file_state` (`file_state_id`, `state_name`) VALUES
(1, 'uploading'),
(2, 'uploaded'),
(3, 'deleted'),
(4, 'uploadFail'),
(5, 'recycled');
