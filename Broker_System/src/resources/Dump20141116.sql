CREATE DATABASE  IF NOT EXISTS `netsec` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `netsec`;
-- MySQL dump 10.13  Distrib 5.6.17, for Win64 (x86_64)
--
-- Host: localhost    Database: netsec
-- ------------------------------------------------------
-- Server version	5.6.21-enterprise-commercial-advanced-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bill_details_amazon`
--

DROP TABLE IF EXISTS `bill_details_amazon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bill_details_amazon` (
  `bill_no` varchar(45) NOT NULL,
  `number_of_items_sold` int(11) NOT NULL,
  `item_details` varchar(45) NOT NULL,
  KEY `amazon_bill_idx` (`bill_no`),
  CONSTRAINT `amazon_bill` FOREIGN KEY (`bill_no`) REFERENCES `broker_transaction_amazon` (`bill_no`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bill_details_amazon`
--

LOCK TABLES `bill_details_amazon` WRITE;
/*!40000 ALTER TABLE `bill_details_amazon` DISABLE KEYS */;
/*!40000 ALTER TABLE `bill_details_amazon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `broker_details_amazon`
--

DROP TABLE IF EXISTS `broker_details_amazon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `broker_details_amazon` (
  `broker_id` varchar(45) NOT NULL,
  `broker_name` varchar(45) NOT NULL,
  `shared_key` varchar(999) NOT NULL,
  `session_key` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`broker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `broker_details_amazon`
--

LOCK TABLES `broker_details_amazon` WRITE;
/*!40000 ALTER TABLE `broker_details_amazon` DISABLE KEYS */;
/*!40000 ALTER TABLE `broker_details_amazon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `broker_transaction_amazon`
--

DROP TABLE IF EXISTS `broker_transaction_amazon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `broker_transaction_amazon` (
  `order_num` varchar(45) NOT NULL,
  `bill_no` varchar(45) NOT NULL,
  `broker_id` varchar(45) DEFAULT NULL,
  `amount` decimal(5,2) NOT NULL,
  `date` datetime NOT NULL,
  `status` varchar(45) NOT NULL,
  `payment_rxd_dat` datetime DEFAULT NULL,
  PRIMARY KEY (`bill_no`),
  KEY `broker_amazon_idx` (`broker_id`),
  CONSTRAINT `broker_amazon` FOREIGN KEY (`broker_id`) REFERENCES `broker_details_amazon` (`broker_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `broker_transaction_amazon`
--

LOCK TABLES `broker_transaction_amazon` WRITE;
/*!40000 ALTER TABLE `broker_transaction_amazon` DISABLE KEYS */;
/*!40000 ALTER TABLE `broker_transaction_amazon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_summary_paypal`
--

DROP TABLE IF EXISTS `order_summary_paypal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_summary_paypal` (
  `order_num` int(11) NOT NULL,
  `user_id` varchar(45) NOT NULL,
  `status_of_pay` varchar(45) NOT NULL,
  `date_paid` datetime DEFAULT NULL,
  `user_signature` varchar(999) NOT NULL,
  `vendor_signature_ack` varchar(999) NOT NULL,
  PRIMARY KEY (`order_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_summary_paypal`
--

LOCK TABLES `order_summary_paypal` WRITE;
/*!40000 ALTER TABLE `order_summary_paypal` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_summary_paypal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_details_paypal`
--

DROP TABLE IF EXISTS `user_details_paypal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_details_paypal` (
  `user_id` varchar(45) NOT NULL,
  `user_name` varchar(45) NOT NULL,
  `user_credit_available` int(11) DEFAULT NULL,
  `user_public_key` varchar(999) NOT NULL,
  `type` varchar(45) NOT NULL,
  `user_private_key` varchar(999) NOT NULL,
  `session_key` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_details_paypal`
--

LOCK TABLES `user_details_paypal` WRITE;
/*!40000 ALTER TABLE `user_details_paypal` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_details_paypal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendor_inventory_amazon`
--

DROP TABLE IF EXISTS `vendor_inventory_amazon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vendor_inventory_amazon` (
  `item_no` int(11) NOT NULL,
  `number_items_avail` int(11) NOT NULL,
  `number_sold` int(11) NOT NULL,
  `item_price` decimal(5,2) NOT NULL,
  PRIMARY KEY (`item_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_inventory_amazon`
--

LOCK TABLES `vendor_inventory_amazon` WRITE;
/*!40000 ALTER TABLE `vendor_inventory_amazon` DISABLE KEYS */;
/*!40000 ALTER TABLE `vendor_inventory_amazon` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-11-16 17:18:47
