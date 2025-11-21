library("ggplot2")

# read csv file
results_subs = read.csv("/home/victor/workspace/silbops-benchmark/test_results_subs_8_noviembre.csv")
results_subs = aggregate(results_subs[,2:7], list(results_subs$broker, results_subs$subs), mean)

results_entities = read.csv("/home/victor/workspace/silbops-benchmark/test_results_entities_8_noviembre")
results_entities = aggregate(results_entities[, 2:7], list(results_entities$broker, results_entities$entities), mean)

results_notifications = read.csv("/home/victor/workspace/silbops-benchmark/test_results_notifications_8_noviembre.csv")
results_notifications = aggregate(results_notifications[, 2:7], list(results_notifications$broker, results_notifications$notifs), mean)

# =================================== # Subscriptions # =================================== #

# Throughput
ggplot(results, aes(x=results$Group.2, y=throughput, group=results$Group.1, colour=results$Group.1))+
  geom_line()+
  geom_point()+
  xlab("Number of subscriptions")+
  ylab("Throughput (notifications/s)")+
  ggtitle("Throughput measured sending 100 k notifications at 1000 notifications/s")+
  guides(colour=guide_legend(title="Broker"))

# Delay
ggplot(results, aes(x=subs, y=avg_delay, group=results$Group.1, colour=results$Group.1))+
  geom_line()+
  geom_point()+
  xlab("Number of subscriptions")+
  ylab(expression(paste("Delay (", mu , "s)")))+
  ggtitle("Average delay measured sending 100 k notifications at 1000 notifications/s")+
  guides(colour=guide_legend(title="Broker"))

# =================================== # Notifications # =================================== #

# Throughput
ggplot(results_notifications, aes(x=notifs, y=throughput, colour=Group.1))+
  geom_line()+
  geom_point()+
  xlab("Number of notifications")+
  ylab("Throughput (notifications/s)")+
  ylim(0, 250)+
  ggtitle("Throughput measured sending 100 k notifications at 1000 notifications/s")+
  guides(colour=guide_legend(title="Broker"))

# Delay
ggplot(results_subs, aes(x=notifs, y=avg_delay, group=notif_speed, colour=Group.1))+
  geom_line()+
  geom_point()+
  xlab("Number of notifications")+
  ylab("Delay (ms)")+
  ggtitle("Average delay measured sending 100 k notifications at 1000 notifications/s")+
  guides(colour=guide_legend(title="Broker"))

# =================================== # Entities # =================================== #

# Throughput
ggplot(results_entities, aes(x=entities, y=throughput, colour=Group.1))+
  geom_line()+
  geom_point()+
  xlab("Number of Context-Broker entites")+
  ylab("Throughput (notifications/s)")+
  ggtitle("Throughput measured sending 100 k notifications at 1000 notifications/s")+
  guides(colour=guide_legend(title="Broker"))

# Delay
ggplot(results_entities, aes(x=entities, y=avg_delay, colour=Group.1))+
  geom_line()+
  geom_point()+
  xlab("Number of Context-Broker entites")+
  ylab("Delay (ms)")+
  ggtitle("Average delay measured sending 100 k notifications at 1000 notifications/s")+
  guides(colour=guide_legend(title="Broker"))

# =================================== # SilboPS (mocked Context-Broker) # =================================== #
library("tikzDevice")
results_cb = read.csv("/home/victor/workspace/silbops-benchmark/final_results.csv")
results_cb$notif_speed <- as.factor(results_cb$notif_speed)

#hline <- data.frame(yint = c(8902.1542, 13695.5722) ,lt = c('1 Context-Broker', '2 Context-Broker')) 

tikz(file = "./static_topology_cluster.tex", standAlone=TRUE, width = 10, height = 10)
plot1 <- ggplot(results_cb, aes(x=results_cb$subs, y=results_cb$throughput, group=results_cb$broker, colour=results_cb$broker))+
  geom_line()+
  #geom_hline(data = hline,aes(yintercept=yint,color=lt, group=lt),size=0.3, linetype='dashed',show.legend = TRUE)+
  geom_point()+
  scale_x_log10(breaks=c(1, 10, 100, 1000, 10000), labels=c("1", "10", "100", "1 k", "10 k"), limits=c(1, 10000))+
  scale_y_log10(breaks=c(100, 500, 1000, 5000, 10000, 15000, 12000, 8000, 2500, 4000, 9000, 13500))+
  xlab("Number of subscriptions")+
  ylab("Throughput (notifications/s)")+
  ggtitle("Throughput measured sending 1M notifications")+
  guides(colour=guide_legend(title="Broker"))

print(plot1)

dev.off()
tools::texi2dvi('./static_topology_cluster.tex',pdf=T)