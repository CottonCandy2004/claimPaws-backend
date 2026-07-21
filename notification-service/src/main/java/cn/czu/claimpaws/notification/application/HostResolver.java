package cn.czu.claimpaws.notification.application;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;

@FunctionalInterface
public interface HostResolver {
    InetAddress[] resolve(String host) throws UnknownHostException;
}
