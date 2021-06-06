package info.nemoworks.udo.messaging;

import com.google.common.reflect.TypeToken;
import info.nemoworks.udo.messaging.gateway.K8sGateway;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import org.junit.Test;

import java.io.IOException;

public class K8sJavaClientTest {

    @Test
    public void testGetNode() throws IOException, ApiException {
        K8sGateway k8sGateway = new K8sGateway();
        k8sGateway.getNodeList();
    }

    @Test
    public void testJavaClient() throws IOException, ApiException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listNamespacedPod("calico-system", null, Boolean.FALSE, null
                , null, null, 10, null, null
                , null);
        V1Pod pod = api.readNamespacedPod("calico-node-4p4vd", "calico-system", null, null, null);
        String kind = pod.getKind();
        V1ObjectMeta podMetadata = pod.getMetadata();
        V1PodStatus podStatus = pod.getStatus();
        System.out.println(pod.toString());
        //   V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
//        for (V1Pod item : list.getItems()) {
//            System.out.println(item.getMetadata().getName());
//        }
    }

    @Test
    public void testJavaClientGetNamespace() throws IOException, ApiException {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        Watch<V1Namespace> watch = Watch.createWatch(
                client,
                api.listNamespaceCall(null,null,null,null,null,5
                ,null,null,Boolean.TRUE,null),
                new TypeToken<Watch.Response<V1Namespace>>(){}.getType());

        for (Watch.Response<V1Namespace> item : watch) {
            System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
        }
    }

}
