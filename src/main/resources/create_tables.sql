DROP TABLE IF EXISTS `receipt`;
DROP TABLE IF EXISTS `receipt_item`;

CREATE TABLE `receipt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `store_name` varchar(50) NOT NULL,
  `transaction_date` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  INDEX `idx_receipt_store_name` (`store_name`)
);


CREATE TABLE `receipt_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `receipt_id` bigint(20) NOT NULL,
  `item` varchar(100) NOT NULL,
  `price` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `receipt_item_ibfk_1` (`receipt_id`),
  CONSTRAINT `receipt_item_ibfk_1` FOREIGN KEY (`receipt_id`) REFERENCES `receipt` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
);