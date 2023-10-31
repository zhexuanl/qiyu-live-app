# 进入从数据库
# 注意：执行完此步骤后退出主数据库，防止再次操作导致 File 和 Position 的值发生变化# 验证 slave 用户是否可用
# 查询 server_id 值
show variables like 'server_id';


# 若之前设置过同步，请先重置
stop slave;
reset slave;

# The server_id is a unique identifier for each server in a MySQL replication setup. It's important that every server (both master and slave) has a unique server_id. If you don't specify one, it defaults to 0, and this default setting does not allow replication.
#
# Here's why it might not be in your example:
#
#     Pre-configured in my.cnf: Often, in docker setups or in managed environments, the server_id might already be set in the MySQL configuration file (my.cnf or my.ini). For example, you can specify it as:
#
#     ini
#
# [mysqld]
# server_id=1
#
# If you've set it in the configuration file, you don't need to set it again at runtime. In Docker environments, setting it in the configuration file is more persistent and resilient to restarts.
SET GLOBAL SERVER_ID = 2;


-- Configure the replication from the master
CHANGE MASTER TO
    MASTER_HOST ='cloud-mysql-master-1',
    MASTER_USER ='mysql-slave',
    MASTER_PASSWORD ='mysql-000722',
    MASTER_LOG_FILE ='mysql-bin.000001',
    MASTER_LOG_POS =157;
-- Start replication
START SLAVE;
# 查询 Slave 状态
show slave status;

# Why no port is specified:
#
# In a Docker Compose environment, when two services are connected in the same network, they can communicate with each other using the service name (in this case, mysql-master) and the default port. If your master MySQL is running on the default port (3306), then you don't need to specify it. The slave will attempt to connect to the master on port 3306 by default.
#
# However, if you have changed the master's MySQL port to something other than 3306, then you'd need to specify the port using the MASTER_PORT parameter:
#
# sql
#
#     MASTER_PORT=your_master_port
#
#     Why MASTER_HOST is a service name and not an IP:
#
#     In Docker and Docker Compose, when you create a network, Docker provides DNS resolution for services to communicate using their service names rather than IP addresses. This is especially useful because IP addresses can change, but service names remain consistent.
#
#     For example, if you have a service named mysql-master in your docker-compose.yml file, other services in the same Docker network can refer to this service using the name mysql-master rather than its IP address.
#
#     If you're using Docker Compose and have defined two services mysql-master and mysql-slave, the slave can simply use mysql-master as the host name to communicate with the master, assuming they are in the same network.
#
# It simplifies service-to-service communication and ensures your configurations are more stable, since service names won't change, but IP addresses might.

