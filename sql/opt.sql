

drop table if exists `train_seat`;
create table `train_seat` (
  `id` bigint not null comment 'id',
  `train_code` varchar(20) not null comment '车次编号',
  `carriage_index` int not null comment '厢序',
  `row_index` char(2) not null comment '排号|01, 02',
  `col_index` char(1) not null comment '列号|枚举[SeatColEnum]',
  `seat_type` char(1) not null comment '座位类型|枚举[SeatTypeEnum]',
  `carriage_seat_index` int not null comment '同车厢座序',
  `create_time` datetime(3) comment '新增时间',
  `update_time` datetime(3) comment '修改时间',
  primary key (`id`)
) engine=innodb default charset=utf8mb4 comment='座位';


drop table if exists `daily_train_seat`;
create table `daily_train_seat` (
  `id` bigint not null comment 'id',
  `date` date not null comment '日期',
  `train_code` varchar(20) not null comment '车次编号',
  `carriage_index` int not null comment '箱序',
  `row_index` char(2) not null comment '排号|01, 02',
  `col_index` char(1) not null comment '列号|枚举[SeatColEnum]',
  `seat_type` char(1) not null comment '座位类型|枚举[SeatTypeEnum]',
  `carriage_seat_index` int not null comment '同车箱座序',
  `sell` varchar(50) not null comment '售卖情况|将经过的车站用01拼接，0表示可卖，1表示已卖',
  `create_time` datetime(3) comment '新增时间',
  `update_time` datetime(3) comment '修改时间',
  primary key (`id`)
) engine=innodb default charset=utf8mb4 comment='每日座位';


ALTER TABLE daily_train_ticket ADD COLUMN version INT DEFAULT 0;
ALTER TABLE daily_train_seat ADD COLUMN version INT DEFAULT 0;
