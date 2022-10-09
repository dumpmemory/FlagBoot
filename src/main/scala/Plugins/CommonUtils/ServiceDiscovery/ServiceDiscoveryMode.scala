package Plugins.CommonUtils.ServiceDiscovery

sealed trait ServiceDiscoveryMode

sealed trait ServiceDiscoveryCommunicateMode extends ServiceDiscoveryMode
case class LocalServiceDiscoveryCommunicateMode() extends ServiceDiscoveryCommunicateMode
case class RedisServiceDiscoveryCommunicateMode() extends ServiceDiscoveryCommunicateMode

sealed trait ServiceDiscoveryInfoMode extends ServiceDiscoveryMode
case class ServiceDiscoveryHostnameInfoMode() extends ServiceDiscoveryInfoMode