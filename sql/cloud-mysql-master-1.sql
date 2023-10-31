#主数据库创建用户 slave 并授权
# 创建用户,设置主从同步的账户名
create user 'mysql-slave'@'%' identified with mysql_native_password by 'mysql-000722';
# 授权
grant replication slave on *.* to 'mysql-slave'@'%';
# 刷新权限
flush privileges;
# 查询 server_id 值
show variables like 'server_id';

# 查询 Master 状态，并记录 File 和 Position 的值，这两个值用于和下边的从数据库中的 change 那条 sql 中的 master_log_file，master_log_pos 参数对齐使用
show master status;

show binlog events;
# 重置下 master 的 binlog 位点
reset master;

# DROP USER 'qiyu-slave'@'%';
