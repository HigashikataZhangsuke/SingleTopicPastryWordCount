# SingleTopicPastryWordCount

## 师兄这是我写的用于测试单Topic情况下WordCount性能的Code。按文件可以分为如下几部分：

1.BootNodeentry 和 OtherNodeentry：这两者是用于创建Node，指导每个Node实现读取数据等行为的外层类，作用类似于CommonAPItest。具体来讲，BootNodeentry 用于生成Ring中的第一个node，也就是BootNode。它还会第一个生成Topic。执行顺序严格按照3步逻辑走：(1)生成BootNode及其scribe client，休眠一段时间后，等到所有node都加入了Pastry Ring之后，进入(2)生成Topic并scribe它，同时读取数据，获得自己的childlist。休眠一段时间后，等到所有node都scribe了这个Topic之后，进入(3)检测自己是否是Root，如果是的话开始指导整个scribe Tree上传数据。
OtherNodeentry是生成其他节点的类：它也按照类似的3步逻辑：(1)生成若干Node及其scribe client，休眠一段时间后，等到所有node都加入了Pastry Ring之后，进入(2)让自己的每个Node都scribe到BootNode对应的Topic，休眠一段时间后，读取数据，让每个Node获得自己的childlist。进入(3)检测自己生成的Node中是否有Root，如果是的话Root开始指导整个scribe Tree上传数据。

2、WordCountApplication：这是Scribe Client的实现。主要实现的方法有：
getchildlist:让对应Node获得它在当前Scribe Tree中的child，并记录在hashmap中。
readdata：让对应Node读取数据，并初始化在LinkedHashmap中。
run50：让对应Node按照给定的量上传一批数据到自己的Parent Node
check：如果当前Node是某些Node的Parent，那么check函数可以让他们明确自己目前是否收到了足够的数据，可以上传给自己的Parent
checkmarker：每一个节点上传最后一次数据时，都会带有一个marker，checkmarker函数可以让Root检测是否收到了足够的marker，从而确定这一轮WordCount是否成功。
StartPublishTask/StartPublishTaskLeaf：用于给Root和叶子节点设定Timer，让他们按时按间隔开始计算
logger/loggererror：用于记录logger的函数
sendupdaterequest：Root节点使用该函数向树的每个成员发布开始计算的消息
Topic level的deliver函数：树里每个成员收到publish消息后的回调函数，这里检测是否是第一个window，如果不是，将会给每个Node都重置自己的childlist
Application level的deliver函数：收到其他Node消息时候的回调函数。这里考虑到只有Parent节点或者Root节点会收到带有数据消息，因此在函数的第一部分总是让Node将收到的数据合并，并更新自己的childlist。之后根据自己是parent node还是root 进行对应的检测，判断是否继续上传或是记录本轮计算结束的时间。如果收到的是其他通知上传自己数据的消息的话，也会调用对应的函数进行操作。

重要的类成员：
SelfDict：用于记录当前Node所有数据的LinkedHashMap
Childlist：用于记录当前Node所有child Id以及对应child上传了多少次数据的linkedHashmap

3、所有的Msg：用于传递消息，包括scribe content和message两类。MsgofRequestUpdate是Scribe Content，用于在每一轮的开始时由Root发布，通知所有Node本轮计算开始；Startwindow是message类型，用于root自己的timer，timer每隔一定时间就用这一msg让Root开始通知一个window的计算；selfschedule50msg是给叶子节点使用的msg，叶子节点在每次收到这一消息时，都会运行run50函数，上传一批数据给自己的Parent节点；Bottomnodemsg是用于传输数据的类型，它携带消息发送者以及发送的数据两个信息。

4、params：Pastry采用的参数：目前修改的地方只有max_open_sockets一项，其余都和默认设置相同。

5、GCPWC.sh 和 startbootnode.sh：两个类似于pssh的脚本，用于在VM上运行代码，生成Code。其参数按顺序分别为：这个VM第一个Node将会bind的端口；boot IP； boot port；这个VM上一共会生成多少Node；Wordcount的window size；数据读取速率；本次实验一共有多少Node同时运行。

## 有关测试中遇到的Warning 和 Error：
测试中出现了两个Warning：
1、LimitedSockets类提示某些node的socket中没有reader
2、ScribeImpl类提示有些scribe client从未知的Topic收到了publish消息（但是只有一个Topic）
以及一个error：
1、Scribe client提示某些Node在收到其他node传来的消息时，并未在自己的childlist中找到这一node。导致出现null pointer错误（因为要给对应child的计数增加1，get方法返回了这一空指针错误）

对应的错误发生在wordcountapplication的319-323行，也就是application level deliver函数处。

以下是实验中硬件和网络带宽资源的消耗情况。
![Screenshot from 2023-02-19 22-39-52](https://user-images.githubusercontent.com/32588848/220004430-a26735a2-b95c-48d8-a951-3afc49054a3f.png)
![Screenshot from 2023-02-19 22-40-35](https://user-images.githubusercontent.com/32588848/220004432-1456a84e-7640-49c1-99ca-eaed027a835e.png)
![Screenshot from 2023-02-19 22-41-16](https://user-images.githubusercontent.com/32588848/220004433-4696b1e1-9184-4e14-ba59-eaf7659d92bf.png)
