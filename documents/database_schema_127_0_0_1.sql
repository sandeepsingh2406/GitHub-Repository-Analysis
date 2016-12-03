-- phpMyAdmin SQL Dump
-- version 4.6.4
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 03, 2016 at 10:32 AM
-- Server version: 5.7.14
-- PHP Version: 5.6.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `cs441project`
--
CREATE DATABASE IF NOT EXISTS `cs441project` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `cs441project`;

-- --------------------------------------------------------

--
-- Table structure for table `alllanguagerepotable`
--

CREATE TABLE `alllanguagerepotable` (
  `repoName` varchar(500) NOT NULL,
  `repoID` bigint(20) NOT NULL,
  `ownerUserName` varchar(500) NOT NULL,
  `ownerID` bigint(20) NOT NULL,
  `createdAt` date NOT NULL,
  `updatedAt` date NOT NULL,
  `watchersCount` int(11) NOT NULL,
  `forksCount` int(11) NOT NULL,
  `openIssue` int(11) NOT NULL,
  `repoSize` bigint(20) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `toprepocommitstable`
--

CREATE TABLE `toprepocommitstable` (
  `repoName` varchar(500) NOT NULL,
  `repoID` bigint(20) NOT NULL,
  `numberOfCommits` int(11) NOT NULL,
  `numberOfFiles` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `toprepolanguagetable`
--

CREATE TABLE `toprepolanguagetable` (
  `repoName` varchar(500) NOT NULL,
  `repoID` bigint(20) NOT NULL,
  `language` varchar(20) NOT NULL,
  `numberOfLines` bigint(20) NOT NULL,
  `numberOfFiles` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `usertable`
--

CREATE TABLE `usertable` (
  `userName` varchar(500) NOT NULL,
  `userID` bigint(20) NOT NULL,
  `publicReposCount` int(11) NOT NULL,
  `followersCount` int(11) NOT NULL,
  `followingCount` int(11) NOT NULL,
  `subscriptionsCount` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `alllanguagerepotable`
--
ALTER TABLE `alllanguagerepotable`
  ADD PRIMARY KEY (`repoName`,`repoID`);

--
-- Indexes for table `toprepocommitstable`
--
ALTER TABLE `toprepocommitstable`
  ADD PRIMARY KEY (`repoName`,`repoID`);

--
-- Indexes for table `toprepolanguagetable`
--
ALTER TABLE `toprepolanguagetable`
  ADD PRIMARY KEY (`repoName`,`repoID`,`language`);

--
-- Indexes for table `usertable`
--
ALTER TABLE `usertable`
  ADD PRIMARY KEY (`userName`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
