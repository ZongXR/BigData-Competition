<h1>一键自动安装大数据集群</h1>
<img src="https://hadoop.apache.org/hadoop-logo.jpg" alt="hadoop">
<img src="https://spark.apache.org/images/spark-logo-rev.svg" alt="spark" width="141" height="72">
<img alt="Apache Hive" src="https://hive.apache.org/images/hive_logo_medium.jpg">
<h2>关于此项目</h2>
<h3>为什么做了这么一个项目？</h3>
因为部分初学者对于安装配置大数据集群环境感到苦恼，所以做了这么一个解放人力的自动化脚本。能够省去安装过程中的诸多麻烦。
<h3>这个项目有什么用</h3>
使用它能一键自动安装大数据集群环境，自动给你配置环境变量，自动给你配置hosts，自动给你配置免密登录，自动给你做其他配置，非常方便。从基础环境搭建，到zookeeper，再到hadoop，再到mysql，再到hive，再到spark，一键完成，自动部署。
<h2>使用方法</h2>
<ol>
<li>使用<a href="http://mirrors.aliyun.com/centos/7/isos/x86_64/CentOS-7-x86_64-Minimal-2009.iso" target="_blank">此镜像</a>，安装好集群环境，记录下各个节点的ip</li>
<li>将master.sh放入主节点的/home/xxx目录下；将slave.sh放入从节点的/home/xxx目录下；将start-master.sh放入主节点/home/xxx目录下；将start-slave.sh放入从节点/home/xxx目录下；将hadoop目录放入主节点/home/xxx路径下；将hive目录放入主节点/home/xxx路径下。xxx可自定义</li>
<li>
对于从节点执行以下代码<br />
<code>sed -i 's/\r$//' ./*.sh</code><br />
<code>chmod +x ./*.sh</code><br />
<code>source ./slave.sh</code>
</li>
<li>
对于主节点执行以下代码，其中master.sh的参数为各个从节点的ip<br />
<code>sed -i 's/\r$//' ./*.sh</code><br />
<code>chmod +x ./*.sh</code><br />
<code>source ./master.sh 192.168.137.242 192.168.137.190 192.168.137.178</code>
</li>
<li>
安装完毕后，刷新环境变量<br />
<code>source /etc/profile</code>
</li>
<li>
安装完毕后，对于从节点执行以下代码<br />
<code>./start-slave.sh</code>
</li>
<li>
安装完毕后，对于主节点执行以下代码<br />
<code>./start-master.sh</code>
</li>
<li>
运行com.spark.test.WordCount<br />
或者使用maven打jar包，将jar包上传至master节点。通过以下命令运行<br />
<code>spark-submit --class com.spark.test.WordCount ./WordCount-1.0-SNAPSHOT.jar 你的文件路径</code>
</li>
</ol>
<h2>怎么算部署成功了?</h2>
所有节点运行<code>jps</code>命令，与下表后台任务对照，对照一致则成功。
<h3>主节点</h3>
<table title="主节点">
<tr>
<th>任务名</th><th>由谁启动</th>
</tr>
<tr>
<td>Jps</td><td>java</td>
</tr>
<tr>
<td>QuorumPeerMain</td><td>zookeeper</td>
</tr>
<tr>
<td>NameNode</td><td>hadoop</td>
</tr>
<tr>
<td>SecondaryNameNode</td><td>hadoop</td>
</tr>
<tr>
<td>ResourceManager</td><td>hadoop</td>
</tr>
<tr>
<td>Master</td><td>spark</td>
</tr>
</table>
<h3>从节点</h3>
<table title="从节点">
<tr>
<th>任务名</th><th>由谁启动</th>
</tr>
<tr>
<td>Jps</td><td>java</td>
</tr>
<tr>
<td>QuorumPeerMain</td><td>zookeeper</td>
</tr>
<tr>
<td>DataNode</td><td>hadoop</td>
</tr>
<tr>
<td>NodeManager</td><td>hadoop</td>
</tr>
<tr>
<td>RunJar</td><td>hive</td>
</tr>
<tr>
<td>Worker</td><td>spark</td>
</tr>
</table>
<h2>更新日志</h2>
<table>
<tr>
<th>版本</th><th>更新内容</th><th>更新日期</th>
</tr>
<tr>
<td>0.0.1.0</td><td>配置主从节点hosts文件；配置主节点免密登录；配置主从节点防火墙</td><td>2021年10月31日</td>
</tr>
<tr>
<td>0.0.2.0</td><td>配置时区同步服务；配置jdk</td><td>2021年10月31日</td>
</tr>
<tr>
<td>0.1.0.0</td><td>配置安装zookeeper；添加启动脚本</td><td>2021年10月31日</td>
</tr>
<tr>
<td>0.1.1.0</td><td>配置安装hadoop</td><td>2021年11月1日</td>
</tr>
<tr>
<td>0.1.2.0</td><td>从节点配置安装mysql</td><td>2021年11月2日</td>
</tr>
<tr>
<td>0.1.3.0</td><td>安装hive</td><td>2021年11月5日</td>
</tr>
<tr>
<td>0.1.4.0</td><td>配置并启动Hive</td><td>2021年11月6日</td>
</tr>
<tr>
<td>0.1.5.0</td><td>hive连接mysql提供jdbc；安装配置scala</td><td>2021年11月7日</td>
</tr>
<tr>
<td>0.1.6.0</td><td>修复若干bug；安装配置spark</td><td>2021年11月7日</td>
</tr>
<tr>
<td>0.1.7.0</td><td>修复bug；spark启动失败</td><td>2021年11月13日</td>
</tr>
<tr>
<td>0.1.8.0</td><td>修复bug；spark不显示web管理界面</td><td>2021年11月14日</td>
</tr>
<tr>
<td>0.1.9.0</td><td>设置主机名为不同名称</td><td>2021年11月15日</td>
</tr>
<tr>
<td>1.0.0.0</td><td>scala版本改为2.11.11</td><td>2021年11月16日</td>
</tr>
<tr>
<td>1.1.0.0</td><td>添加WordCount的Spark任务</td><td>2021年11月17日</td>
</tr>
<tr>
<td>1.2.0.0</td><td>通过RDD的方式执行WordCount任务</td><td>2021年11月17日</td>
</tr>
<tr>
<td>1.3.0.0</td><td>通过DataFrame的方式执行WordCount任务</td><td>2021年11月17日</td>
</tr>
<tr>
<td>1.3.1.0</td><td>优化执行方式；添加hadoop-client依赖</td><td>2021年11月17日</td>
</tr>
<tr>
<td>1.4.0.0</td><td>添加电信基站案例</td><td>2021年11月20日</td>
</tr>
<tr>
<td>1.4.1.0</td><td>禁用hdfs权限，dfs.permissions设置false</td><td>2021年11月20日</td>
</tr>
<tr>
<td>1.5.0.0</td><td>添加JsonFile案例</td><td>2021年11月21日</td>
</tr>
<tr>
<td>1.6.0.0</td><td>添加Dataset案例</td><td>2021年11月22日</td>
</tr>
<tr>
<td>1.7.0.0</td><td>完善手机基站案例</td><td>2021年11月28日</td>
</tr>
<tr>
<td>1.8.0.0</td><td>补充淘宝行为记录案例</td><td>2021年11月30日</td>
</tr>
<tr>
<td>1.8.1.0</td><td>新增淘宝数据案例的DataFrame实现</td><td>2021年11月30日</td>
</tr>
<tr>
<td>1.9.0.0</td><td>WordCount案例分别使用RDD, Dataset, Spark SQL三种方式实现；淘宝数据案例使用RDD, Spark SQL实现</td><td>2021年12月1日</td>
</tr>
<tr>
<td>1.9.1.0</td><td>补充淘宝数据案例的Dataset实现</td><td>2021年12月1日</td>
</tr>
<tr>
<td>1.10.0.0</td><td>通过SparkSQL实现手机基站案例</td><td>2021年12月2日</td>
</tr>
<tr>
<td>1.10.1.0</td><td>通过DataFrame实现手机基站案例</td><td>2021年12月3日</td>
</tr>
<tr>
<td>1.10.2.0</td><td>补充RDD[Row]转DataFrame方法</td><td>2021年12月9日</td>
</tr>
<tr>
<td>1.11.0.0</td><td>补充身高年龄案例</td><td>2021年12月11日</td>
</tr>
</table>
<h2>项目经验</h2>
<ol>
<li>安装的时候尽量避免多次<code>hadoop namenode -format</code>因为重复格式化会造成主从节点的数据版本号不一致，需要修改一致了才能正常运行。</li>
<li>如果scala运行时莫名其妙报错Exception in thread "main" org.apache.spark.SparkException，可以先打包再运行试试。并且建议IDEA直接连接spark调试每次运行前都打包，否则结果可能和代码不一致。使用spark-submit没这个问题</li>
<li>如果直接在IDEA中调试spark，需要加上conf.setJars和conf.setIfMissing并保证开发环境与spark环境能互相ping通。否则会报WARN TaskSchedulerImpl: Initial job has not accepted any resources</li>
<li>map是对每一个元素进行操作；flatMap是对每一个元素操作后再展平，仅适用于返回结果是List或元组；mapPartitions是对每一个分片进行处理，输入输出是整个分片对应的iterator；mapPartitionsWithIndex是在此基础上增加分区号；mapValues搭配分组使用，输入是每组value组成的一个元组</li>
<li>groupByKey输出value是同组各value组成的元组；groupBy输出的value是同一组的key, value；分组与聚合总体的输出包含key</li>
<li>sortBy是对x扩展出来的矩阵按照某一列排序，而不是对x排序</li>
<li>在hadoop的map-reduce中，map相当于对每个元素操作，shuffle相当于分组，reduce相当于聚合</li>
<li>如果用Dataset读取表读取到一堆null，有可能是数据类型与schema给定类型不一致</li>
<li>如果涉及到列的类型变化的适合用RDD做法和DataFrame做法，如果都是SQL的常用操作适合使用Spark SQL做法，如果只有一列适合用RDD做法</li>
</ol>