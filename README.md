<h1>一键自动安装hadoop</h1>
<img src="https://hadoop.apache.org/hadoop-logo.jpg" alt="hadoop">
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
</table>