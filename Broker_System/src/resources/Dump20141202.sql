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
  `bill_no` int(11) NOT NULL,
  `number_of_items_sold` int(11) NOT NULL,
  `item_no` int(11) NOT NULL,
  KEY `amazon_bill_idx` (`bill_no`)
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
  `broker_name` varchar(45) NOT NULL,
  `shared_key` varchar(999) NOT NULL,
  PRIMARY KEY (`broker_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `broker_details_amazon`
--

LOCK TABLES `broker_details_amazon` WRITE;
/*!40000 ALTER TABLE `broker_details_amazon` DISABLE KEYS */;
INSERT INTO `broker_details_amazon` VALUES ('paypal','01ff988c-5eb4-41');
/*!40000 ALTER TABLE `broker_details_amazon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `broker_transaction_amazon`
--

DROP TABLE IF EXISTS `broker_transaction_amazon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `broker_transaction_amazon` (
  `order_num` int(11) DEFAULT NULL,
  `bill_no` int(11) NOT NULL,
  `broker_name` varchar(45) DEFAULT NULL,
  `amount` decimal(5,2) NOT NULL,
  `date` datetime NOT NULL,
  `status` varchar(45) NOT NULL,
  `payment_rxd_dat` datetime DEFAULT NULL,
  PRIMARY KEY (`bill_no`),
  KEY `broker_amazon_idx` (`broker_name`)
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
  `user_name` varchar(45) NOT NULL,
  `status_of_pay` varchar(45) NOT NULL,
  `date_paid` datetime DEFAULT NULL,
  `user_signature` varchar(999) NOT NULL,
  `vendor_signature_ack` varchar(999) NOT NULL,
  `vendor_name` varchar(45) NOT NULL,
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
-- Table structure for table `private_key`
--

DROP TABLE IF EXISTS `private_key`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `private_key` (
  `user` varchar(45) NOT NULL,
  `private_exponent` varchar(900) DEFAULT NULL,
  `private_modulus` varchar(900) DEFAULT NULL,
  PRIMARY KEY (`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `private_key`
--

LOCK TABLES `private_key` WRITE;
/*!40000 ALTER TABLE `private_key` DISABLE KEYS */;
INSERT INTO `private_key` VALUES ('alice','84672717153350577027579138830965403037071755935271783230027972238537031082327334385840751522239366171268592881405349293867585985601861202827911781074871540449665553814335689266397561216241050432794494606670277549183418884077279722235040922974648644221579351877414268822214092571698917327701046054961348065273','116016722712866901521115051359264485769491996168382573175270080420585005666669869198738168395245789148594630504665643132533293946151850842544225594202585293685512597650612041482405087664958571118499033036219690513416158233227644345482452997699057662698885427127233138607507556937766722945648245016886497856471'),('amazon','111565726346908854593250049043750409455439831111632891439144332485406151903789036715811733208329126877433549172457585000201827211443377126789275240730133258596473755567537179376119346053000583995344179230628299954174000352518697655410231700252409224514586995965431869747720214597863470420050582440105483978817','130558773772786557925072379411464931958665789539186915098248345947468224511519420374696954793033694412198677072930963486924399584957316693005530622497558071125922777048868357388498858776694900769443879355838620440181137499462988637577446576573415962936600210326053789012689916156357041325477989223704083899437');
/*!40000 ALTER TABLE `private_key` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `public_key`
--

DROP TABLE IF EXISTS `public_key`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `public_key` (
  `user` varchar(45) NOT NULL,
  `public_exponent` varchar(900) DEFAULT NULL,
  `public_modulus` varchar(900) DEFAULT NULL,
  PRIMARY KEY (`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `public_key`
--

LOCK TABLES `public_key` WRITE;
/*!40000 ALTER TABLE `public_key` DISABLE KEYS */;
INSERT INTO `public_key` VALUES ('alice','65537','116016722712866901521115051359264485769491996168382573175270080420585005666669869198738168395245789148594630504665643132533293946151850842544225594202585293685512597650612041482405087664958571118499033036219690513416158233227644345482452997699057662698885427127233138607507556937766722945648245016886497856471'),('amazon','65537','130558773772786557925072379411464931958665789539186915098248345947468224511519420374696954793033694412198677072930963486924399584957316693005530622497558071125922777048868357388498858776694900769443879355838620440181137499462988637577446576573415962936600210326053789012689916156357041325477989223704083899437');
/*!40000 ALTER TABLE `public_key` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `purchase_history_user`
--

DROP TABLE IF EXISTS `purchase_history_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `purchase_history_user` (
  `bill_no` int(11) DEFAULT NULL,
  `order_num` int(11) DEFAULT NULL,
  `broker_name` varchar(45) DEFAULT NULL,
  `vendor_name` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `purchase_history_user`
--

LOCK TABLES `purchase_history_user` WRITE;
/*!40000 ALTER TABLE `purchase_history_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `purchase_history_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_details_paypal`
--

DROP TABLE IF EXISTS `user_details_paypal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_details_paypal` (
  `user_name` varchar(45) NOT NULL,
  `user_credit_available` int(11) DEFAULT NULL,
  `type` varchar(45) NOT NULL,
  `user_secret_key` varchar(999) NOT NULL,
  PRIMARY KEY (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_details_paypal`
--

LOCK TABLES `user_details_paypal` WRITE;
/*!40000 ALTER TABLE `user_details_paypal` DISABLE KEYS */;
INSERT INTO `user_details_paypal` VALUES ('alice',1000,'customer','a6113f9c-0643-43'),('amazon',0,'vendor','01ff988c-5eb4-41');
/*!40000 ALTER TABLE `user_details_paypal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_info` (
  `uname` varchar(45) NOT NULL,
  `broker` varchar(45) DEFAULT NULL,
  `secKey` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`uname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_info`
--

LOCK TABLES `user_info` WRITE;
/*!40000 ALTER TABLE `user_info` DISABLE KEYS */;
INSERT INTO `user_info` VALUES ('alice','paypal','a6113f9c-0643-43');
/*!40000 ALTER TABLE `user_info` ENABLE KEYS */;
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
  `item_name` varchar(45) NOT NULL,
  PRIMARY KEY (`item_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendor_inventory_amazon`
--

LOCK TABLES `vendor_inventory_amazon` WRITE;
/*!40000 ALTER TABLE `vendor_inventory_amazon` DISABLE KEYS */;
INSERT INTO `vendor_inventory_amazon` VALUES (1,10,5,100.00,'A'),(2,20,20,150.00,'B'),(3,5,4,50.00,'C');
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

-- Dump completed on 2014-12-02 19:32:37
