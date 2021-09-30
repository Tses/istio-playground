# Playground
## Initiation

```bash
mvn io.quarkus.platform:quarkus-maven-plugin:2.2.3.Final:create \
    -DprojectGroupId=gr.tses \
    -DprojectArtifactId=calculator \
    -DclassName="gr.tses.calculator.CalcEngine" \
    -Dpath="/calc
    
    
mvn io.quarkus.platform:quarkus-maven-plugin:2.2.3.Final:create     -DprojectGroupId=gr.tses     -DprojectArtifactId=invoice     -DclassName="gr.tses.invoice.Invoice"     -Dpath="/invoice"
```


```
mvn clean  quarkus:dev -Ddebug=5006 -Dquarkus.http.port=8081
mvn clean  quarkus:dev -Ddebug=5005 -Dquarkus.http.port=8080 -Dgr.tses.calcurl=http://localhost:8081/calc


curl http://localhost:8080/invoice

cd invoice
./mvnw package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/invoice-jvm .


cd calculator
./mvnw package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/calculator-jvm .



skopeo copy docker-daemon:quarkus/invoice-jvm:latest docker://default-route-openshift-image-registry.apps.ocp4.ciet.lab/istio-dp/invoice-jvm:latest 
skopeo copy docker-daemon:quarkus/calculator-jvm:latest docker://default-route-openshift-image-registry.apps.ocp4.ciet.lab/istio-dp/calculator-jvm:v1
```

 **Create service with label selector**
```
kind: Service
apiVersion: v1
metadata:
  name: calculator-jvm
  namespace: istio-dp
  labels:
    app: calculator-jvm
    app.kubernetes.io/component: calculator-jvm
    app.kubernetes.io/instance: calculator-jvm
    app.kubernetes.io/name: calculator-jvm
spec:
  ports:
    - name: http-8080
      protocol: TCP
      port: 8080
      targetPort: 8080
  selector:
    app: calculator-jvm
  
  type: ClusterIP
  
```

```
oc new-app --name invoice-jvm --image-stream=invoice-jvm
oc new-app --name calculator-jvm-1 --image-stream=calculator-jvm:v1
```

http://invoice-istio-dp.apps.ocp4.ciet.lab/invoice

```
a

```

 **Required Labels**

```
 oc patch deploy/invoice-jvm -p '{"spec":{"template":{"metadata":{"labels":{"app":"invoice-jvm","version":"v1"}}}}}'
oc patch deploy/calculator-jvm-1 -p '{"spec":{"template":{"metadata":{"labels":{"app":"calculator-jvm","version":"v1"}}}}}'
```



Protocol selection -> https://istio.io/latest/docs/ops/configuration/traffic-management/protocol-selection/

```
oc patch service/calculator-jvm -p '{"spec":{"ports":[{"name":"http-8080","port":8080}]}}}'
oc patch service/invoice-jvm -p '{"spec":{"ports":[{"name":"http-8080","port":8080}]}}}'
```


**Delete All Istio Related**

```
oc delete VirtualService --all
oc delete DestinationRule --all
oc delete PeerAuthentication --all
oc delete Gateway --all


oc patch deploy/calculator-jvm-1 -p '{"spec":{"template":{"metadata":{"annotations":{"sidecar.istio.io/inject":"false"}}}}}'
oc patch deploy/invoice-jvm -p '{"spec":{"template":{"metadata":{"annotations":{"sidecar.istio.io/inject":"false"}}}}}'
```

*END OF SETUP*

## Setup Istio

**Edit member roll to add namaspace**

**Inject sidecart**

```
oc patch deploy/calculator-jvm-1 -p '{"spec":{"template":{"metadata":{"annotations":{"sidecar.istio.io/inject":"true"}}}}}'
oc patch deploy/invoice-jvm -p '{"spec":{"template":{"metadata":{"annotations":{"sidecar.istio.io/inject":"true"}}}}}'
```

**Do initial test inside cluster**
```
oc run ubi  -i -t   --image=registry.access.redhat.com/ubi8/ubi:latest --restart=Never
curl invoice-jvm:8080/invoice
```


**Gateway**

```
kind: Gateway
apiVersion: networking.istio.io/v1alpha3
metadata:
  name: invoice
  namespace: istio-dp
spec:
  servers:
    - hosts:
        - invoice-istio-dp.apps.ocp4.ciet.lab
      port:
        name: http
        number: 80
        protocol: HTTP
  selector:
    istio: ingressgateway
```

```
kind: VirtualService
apiVersion: networking.istio.io/v1alpha3
metadata:
  name: invoice
  namespace: istio-dp
spec:
  hosts:
    - '*'
  gateways:
    - invoice
  http:
    - route:
        - destination:
            host: invoice-jvm
            port:
              number: 8080   
```


## Mutual TLS

```yaml
kind: PeerAuthentication
apiVersion: security.istio.io/v1beta1
metadata:
  name: default
  namespace: istio-dp
spec:
  mtls:
    mode: STRICT  
```

```bash
oc rsh ubi
# Should not respond
curl invoice-jvm:8080/invoice
exit
oc delete pod ubi

# Start with annotation
oc run ubi  -i -t   --image=registry.access.redhat.com/ubi8/ubi:latest --restart=Never  --overrides='{"metadata":{"annotations":{"sidecar.istio.io/inject":"true"}}}'
# Should run
curl invoice-jvm:8080/invoice
```

### Authorization

Authorize invoice only to call calculate

```bash
oc create serviceaccount invoice
oc patch deploy/invoice-jvm --patch  '{"spec":{"template":{"spec":{"serviceAccountName": "invoice"}}}}'
```

```yaml
kind: AuthorizationPolicy
apiVersion: security.istio.io/v1beta1
metadata:
  name: calculator-jvm
  namespace: istio-dp
spec:
  selector:
    matchLabels:
      app: calculator-jvm
  rules:
    - from:
        - source:
            principals:
              - cluster.local/ns/istio-dp/sa/invoice

```
```bash
# Test from UBI should not run
curl calculator-jvm:8080/calc  
```




## Deployment LAB

```bash
mvn clean  quarkus:dev -Ddebug=5006 -Dquarkus.http.port=8081
./mvnw package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/calculator-jvm:v2 .


skopeo copy docker-daemon:quarkus/calculator-jvm:v2 docker://default-route-openshift-image-registry.apps.ocp4.ciet.lab/istio-dp/calculator-jvm:v2

oc new-app --name calculator-jvm-2 --image-stream=calculator-jvm:v2
```

```bash
oc patch deploy/calculator-jvm-2 -p '{"spec":{"template":{"metadata":{"labels":{"app":"calculator-jvm","version":"v2"}}}}}'
oc patch deploy/calculator-jvm-2 -p '{"spec":{"template":{"metadata":{"annotations":{"sidecar.istio.io/inject":"true"}}}}}'


# Test ????
watch -n .5 curl http://invoice-istio-dp.apps.ocp4.ciet.lab/invoice
```
### A/B

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: calculator-jvm
spec:
  host: calculator-jvm
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2    
```


```yaml
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: calculator-jvm
spec:
  hosts:
  - "calculator-jvm"

  http:
    - route:
      - destination:
          host: calculator-jvm
          subset: v1
        weight: 10
      - destination:
          host: calculator-jvm
          subset: v2
        weight: 90        
```
```
 while : ;do curl -s  http://invoice-istio-dp.apps.ocp4.ciet.lab/invoice  ;echo  ;sleep 0.2; done     
```

### Canary

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: calculator-jvm
spec:
  hosts:
  - "calculator-jvm"
  http:
  - match:
    - headers:
        qa:
          exact: canary-test
    route:
    - destination:
        host: calculator-jvm
        subset: v2

  - route:
    - destination:
        host: calculator-jvm
        subset: v1
```


```yaml
curl -H "qa: canary-test" -s  http://invoice-istio-dp.apps.ocp4.ciet.lab/invoice        
```

## Cirtcuit breaker

Scale to have more than one
```bash
oc scale deploy/calculator-jvm-1 --replicas=2
```

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: calculator-jvm
spec:
  host: calculator-jvm
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2 

    trafficPolicy:

      outlierDetection:
        consecutive5xxErrors: 2        
        interval: 10s
        baseEjectionTime: 30s
```        

Simulate error


```yaml
oc rsh calculator-jvm-2
curl  "localhost:8080/calc/status?e=1&t=1"        
```

