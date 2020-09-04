# BluetoothChat Android App

This app is an implementation of a SpotNet system described in a paper that was accepted for Inforum 2016 conference in Lisbon, Portugal. 

The PDF of the paper can be found [here](https://pdfs.semanticscholar.org/ad20/30eaf65da32199a71b30d200036a96b7652b.pdf) and in the paper folder of this repo. 

## Main Idea

SpotNet's goal is to provide a secure and private decentralized messaging service that challenges traditional cloud-based and increasingly privacy-invasive platforms, e.g. Facebook or WhatsApp. Instead of storing messages and user information in a third-party cloud (which may be subject to inspection without user authorization), SpotNet relies on a short-range Bluetooth communication to exchange messages in a peer-to-peer fashion with nearby smartphones that collectively store and transmit messages to and from each other.

SpotNet addresses the challenges of efficient message forwarding and resource utilization, and uses location-aware routing to select the next hop for message forwarding based on 'closeness' to destination. The closeness is defined by the previous places a given node visited and their physical proximity to the whereabouts of the desired destination node. To define the whereabouts of the nodes we rely on the network of BLE beacons -- small Bluetooth devices that transmit the IDs of their locations to any nearby smartphone which can then be used to refer to specific geographic spots. A SpotNet user will encounter many of these beacons along his way in the city, and SpotNet will record the time as well as frequency of those encounters. Based on this information, a mobility profile of each user is created which is used to effectively route the messages in the network.


## Citing this work

Please use below BibTex code to cite the paper:

```
@inproceedings{zavalyshyn2016efficient,
  title={Efficient Location-aware Message Delivery for Encounter Networks},
  author={Zavalyshyn, Igor and Duarte, Nuno O and Santos, Nuno}
  booktitle={Inforum Simposio de Informatica},
  year={2016}
}

```
