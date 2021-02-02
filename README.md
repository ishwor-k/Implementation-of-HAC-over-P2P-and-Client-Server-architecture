# -Implementation-of-HAC-over-P2P-and-Client-Server-architecture
This is a design and implementation of a simple application layer protocol over UDP to facilitate High Availability Cluster (HAC). HAC has a set of mechanism to detect failovers, network/node failure etc. in order to re-route the traffic to the available systems. 

The designed protocol achieves the following:
a) Detects node failure periodically (**also the Server failure in case of Client-Server mode)
b) Informs the other nodes in the network about the failure (peering option)
c) Detect when the failed node comes back to life
d) Informs other nodes about the availability of new node
