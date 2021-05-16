# R Script needed to create the calibration plot and to aggregate the efficiency scores
# Note that you need to update the "TODO"s for the script to run 

library(plyr)
library(ggplot2)
library(ggpubr)
library(cowplot)
library(gridExtra)
library(ggsci)

ds_name <- "NR-AhR"

# Utility stuff
color_class0 <- "blue"
color_class1 <- "red" 

A2_name <- "CCP[new]"
A1_name <- "CCP[old]"
pool_name <- "CCP[pool]"
ICP_name <- "ICP[old]^new"
AT_name <- "CCP[AT]"
AT2_name <-"CCP[AT2]"

strategy_name_lookup_df <- data.frame(
  "SamplingStrategy"=c("A2","AT","ICP","A1A2mix","A1","A2_CE"), 
  "ss_name" = c(A2_name, AT_name, ICP_name,pool_name, A1_name,AT2_name))

strategy_order = c(A2_name, AT_name, ICP_name,pool_name, A1_name, AT2_name)

# Coloring
my_default_color_palette <- pal_npg()
my_default_color_list <- my_default_color_palette(6)

# Load all data from CSV files
filenames <- Sys.glob(paste0("TODO/tox21_example/run_outputs/strat*",ds_name,".csv"))


all_data = ldply(filenames, function(filename) {
  return(read.csv(filename, header=TRUE))
})

data <- merge(all_data,strategy_name_lookup_df,by="SamplingStrategy")
data$ss_name <- factor(data$ss_name, levels=strategy_order)
names(data) <- tolower(names(data))


# Calibration curves

calib_plot <- ggplot(data,
                     aes(x = confidence, y=accuracy, group=interaction(ss_name),color=ss_name)) +
  # Dashed diagonal
  geom_segment(aes(x = 0, y = 0, xend = 1, yend = 1), color = "black", linetype="dashed") +
  
  geom_line(aes(y=accuracy.0.), color=color_class0) +
  geom_line(aes(y=accuracy.1.), color=color_class1) +
  labs(x="Confidence", y="Accuracy")+
  theme(panel.grid.minor = element_blank()) +
  coord_fixed() +
  scale_x_continuous(breaks = c(0,.2, 0.4, .6, .8, 1.0),
                     labels= c("0.0","0.2", "0.4", "0.6", "0.8","1.0")) +
  scale_y_continuous(breaks = c(0,.2, 0.4, .6, .8, 1.0),
                     labels= c("0.0","0.2", "0.4", "0.6", "0.8","1.0")) +
  facet_wrap(vars(ss_name), labeller = 
               labeller(
                 .rows=label_parsed))

calib_plot

ggsave(paste0("TODO/tox21_example/run_outputs/calibration_plot.",ds_name,".pdf"),
       plot = calib_plot,
       device = "pdf",
       units = "in",
       height = 5.25,
       width = 7)


# Efficiency data

tab <- data %>% select(ss_name,observed.fuzziness) %>% 
  distinct() %>% as.data.frame()

# Sorted in the factor order
with(tab, tab[order(ss_name),])



