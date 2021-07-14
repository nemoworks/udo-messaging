package info.nemoworks.udo.messaging.gateway;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.util.Config;
import java.io.IOException;

public class K8sGateway extends UdoGateway {

    public K8sGateway() throws IOException {
    }

    public void getNodeList() throws IOException, ApiException {
        ApiClient apiClient = Config.defaultClient();
        CoreV1Api api = new CoreV1Api(apiClient);
//        V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, 10, false);
//        nodeList.getItems()
//                .stream()
//                .forEach((node) -> System.out.println(node));
//        V1PodList list = api.listNamespacedPod("calico-system", null, Boolean.FALSE, null
//                , null, null, 10, null, null
//                , null);
        V1Pod pod = api.readNamespacedPod("calico-node-4p4vd", "calico-system", null, null, null);
        String kind = pod.getKind();
        V1ObjectMeta podMetadata = pod.getMetadata();
        V1PodStatus podStatus = pod.getStatus();
        System.out.println(pod.toString());
    }

    @Override
    public void downLink(String tag, byte[] payload) throws IOException, InterruptedException {

    }

    @Override
    public void updateLink(String tag, byte[] payload, String data)
        throws IOException, InterruptedException {

    }
}
