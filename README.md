<h1>一键自动安装hadoop</h1>
<img src="https://hadoop.apache.org/hadoop-logo.jpg" alt="hadoop">
<h2>关于此项目</h2>
<h3>为什么做了这么一个项目？</h3>
因为部分初学者对于安装配置hadoop集群环境感到苦恼，所以做了这么一个解放人力的自动化脚本。能够省去安装过程中的诸多麻烦。
<h3>这个项目有什么用</h3>
使用它能一键自动安装hadoop集群环境，自动给你配置环境变量，自动给你配置hosts，自动给你配置免密登录，自动给你做其他配置，非常方便。
<h2>使用方法</h2>
<ol>
<li>使用<a href="http://mirrors.aliyun.com/centos/7/isos/x86_64/CentOS-7-x86_64-Minimal-2009.iso" target="_blank">此镜像</a>，安装好集群环境，记录下各个节点的ip</li>
<li>将master.sh放入主节点的/home/xxx目录下；将slave.sh放入从节点的/home/xxx目录下；将start.sh放入主节点/home/xxx目录下；将hadoop目录放入主节点/home/xxx路径下。xxx可自定义</li>
<li>
对于从节点执行以下代码<br />
<code>sed -i 's/\r$//' ./slave.sh</code><br />
<code>chmod +x ./slave.sh</code><br />
<code>source ./slave.sh</code>
</li>
<li>
对于主节点执行以下代码，其中master.sh的参数为各个从节点的ip<br />
<code>sed -i 's/\r$//' ./master.sh</code><br />
<code>chmod +x ./master.sh</code><br />
<code>source ./master.sh 192.168.137.242 192.168.137.190 192.168.137.178</code>
</li>
<li>
安装完毕后，对于主节点执行以下代码，start.sh的参数为各个从节点的ip<br />
<code>sed -i 's/\r$//' ./start.sh</code><br />
<code>chmod +x ./start.sh</code><br />
<code>source ./start.sh 192.168.137.242 192.168.137.190 192.168.137.178</code>
</li>
</ol>
<h3>更新日志</h3>
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
</table>