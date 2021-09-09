# DroolsLearn
Drools学习与使用

/**
 * 参照http://3ms.huawei.com/km/blogs/details/8127757 体验在IDEA中如何搭建起基于Drools的规则引擎体验
 * 同时， 参照 https://www.open-open.com/lib/view/open1449191572077.html 来验证规则文件是否支持热加载，即更改规则文件后立即生效
 *
 * 20210909
 * 经过这次试验，发现了两个问题：
 * （1） 通过读Drools的在线帮助文档（https://docs.jboss.org/drools/release/7.33.0.Final/drools-docs/html_single/#architecture-scenarios-ref_decision-management-architecture），
 * 参照2.2.2.1 小节，才知道，原来，如果仅是是一个空的kmodule.xml文件，则规则引擎会遍历执行resources下所有规则引擎——通过这个，才逐步更改
 * kmodule.xml文件和各规则文件，才发现，要求规则文件中的package 后的名称需要与该规则文件所在resources中的目录名层级一致，且与kmodule.xml、
 * 中的packages保持一致，才能执行到规则。之前，好几天运行，程序都没有报错，但是规则没有触发，最开始跟踪过Drools里的执行过程，发现好像是Drools中
 * 在判断我们的java bean类时，发现对应FACT判断不等，原本以为是类加载问题，参照https://issues.redhat.com/browse/DROOLS-1540 这个没有解决，
 * 而且那个问题在7.23.0.Final已解决。这次通过明确怀疑kmodule.xml并试验才解决。
 * （2） 本程序写了个无限循环，然后，在循环中提供重新初始化Drools引擎及Session的方式，然后在这个过程中更新了规则文件，发现不重启程序，新的规则
 * 并不会生效。
