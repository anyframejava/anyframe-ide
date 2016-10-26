drop table board_user;
drop table board_master;
drop table board;

CREATE TABLE board_user(user_id VARCHAR(20) NOT NULL,user_name VARCHAR(50) NOT NULL,password VARCHAR(10) NOT NULL,age NUMERIC(3),cell_phone VARCHAR(14),addr VARCHAR(100),email VARCHAR(50),reg_date DATE,authority VARCHAR(50) NOT NULL,CONSTRAINT PK_BOARD_USER PRIMARY KEY(user_id));
CREATE TABLE board_master(board_master_id INTEGER NOT NULL,title VARCHAR(100) NOT NULL,display_order INTEGER NOT NULL,moderated INTEGER ,CONSTRAINT PK_BOARD_MASTER PRIMARY KEY(board_master_id));
CREATE TABLE board(board_id INTEGER NOT NULL,board_master_id INTEGER NOT NULL,board_name VARCHAR(150) DEFAULT '' NOT NULL,board_desc VARCHAR(255) DEFAULT NULL,board_order INTEGER DEFAULT 1,board_topics INTEGER DEFAULT 0 NOT NULL,reg_date DATE,CONSTRAINT BOARD_PK PRIMARY KEY(board_id,board_master_id),CONSTRAINT BOARD_MASTER_ID_FK FOREIGN KEY(board_master_id) REFERENCES board_master(board_master_id));