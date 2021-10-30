<h1>安装hadoop</h1>
<h2>使用方法</h2>
<ol>
<li>使用<a href="http://mirrors.aliyun.com/centos/7/isos/x86_64/CentOS-7-x86_64-Minimal-2009.iso" target="_blank">此镜像</a>，安装好集群环境，记录下各个节点的ip</li>
<li>将master.sh放入主节点的/home/xxx目录下；将slave.sh放入从节点的/home/xxx目录下。xxx可自定义</li>
<li>
对于主节点执行以下代码，其中master.sh的参数为各个从节点的ip<br />
<code>chmod +x ./master.sh</code><br />
<code>./master.sh 192.168.137.226 192.168.137.135 192.168.137.188</code>
</li>
<li>
对于从节点执行以下代码<br />
<code>chmod +x ./slave.sh</code><br />
<code>./slave.sh</code>
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
</table>