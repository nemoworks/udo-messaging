package info.nemoworks.udo.messaging.gateway;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class K8sGateway extends UdoGateway{

    public void getNodeList() throws IOException, ApiException {
        ApiClient apiClient = Config.defaultClient();
        CoreV1Api api = new CoreV1Api(apiClient);
        V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, 10, false);
        nodeList.getItems()
                .stream()
                .forEach((node) -> System.out.println(node));
        V1PodList list = api.listNamespacedPod("calico-system", null, Boolean.FALSE, null
                , null, null, 10, null, null
                , null);
        V1Pod pod = api.readNamespacedPod("calico-node-4p4vd", "calico-system", null, null, null);
        System.out.println(pod.toString());
    }

    @Override
    public void downlink(String tag, byte[] payload) throws IOException, InterruptedException {

    }
}
