# SSE SAMPLE

## WebApplicationType.Servlet SSE prototype

### Spec
* mvn
* Using Embedded Tomcat container
* JDK 17
* Spring boot 3.2.4
* Spring Web MVC

### 주요 확인 포인트
### 1. Servlet container의 설정과 관련하여 SSE 구동 시 확인하면 좋을 내용
* server.tomcat.accept-count=10 //http-thread가 모두 work 상황일 때 queue의 max size
* server.tomcat.max-connections=6 //tomcat이 최대 감당 가능한 connection 수
* server.tomcat.threads.max=3 //tomcat의 http 요청을 처리할 thread의 최대 수
* server.tomcat.threads.min-spare=3 //tomcat의 http 요청을 바로받아 쓸 수 있는 spare thread의 수
    #### 위와 같이 서버 설정을 한 후 진행했을 경우, sse connection으로 인해, connection이 max에 달할 경우 다른 http 요청을 처리할 수 없는 상태가 된다. 이러한 점을 고려했을 때, sse event를 publish할 트리거를 http-thread 에게 처리하도록 하는 방식에는 한계가 있다. 톰캣의 connection 자체가 불가능하거나, accept-count 등의 설정에 의해 지속적인 pending, 혹은 timeout에 빠질 수 있기 때문에, 이벤트에 대한 트리거는 다른 방식을 채택해야 할 듯하다.
        예시) HTTP GET or POST /publish 는 인스턴스의 상태에 따라 원활히 동작하지 않을 수 있음 

### 2. SSE Event를 구현시 확인하면 좋을 내용
* 실시간으로 client에게 server send event 방식으로 전송 될 데이터 chunck가 client 마다 specific하게 관리가 필요하다면 인스턴스마다 데이터에 대한 동기화가 필요할 듯 하다. 하지만, 굳이 실시간으로 전송 될 데이터가 client에 좌지우지 되지 않는다면 고려하지 않아도 좋을 듯 하다.
* client 사이드에서 event를 streaming 할 때, 부득이하게 에러가 나거나, 브라우저가 닫히는 경우 서버에서는 실시간으로 해당 connection에 대한 close를 할 수 없다는 점을 고려해야 할 듯하다. 이를 위해 event에 timeout을 짧게 줘서, 클라이언트에서 재요청을 하게 끔 유도하여 만료되거나, 유효하지 않은 서버에서 관리하고 있는 SseEmitter를 제거할 수 있도록 한다. 혹은 주기적으로 의미없는 데이터를 흘려보내 client단에 닿지 않는 경우 AsyncRequestNotUsableException를 유도하여 강제적으로 해당 Emiiter를 종료시켜 제거시키는 방식을 택해야 한다.
* http 1.1 protocol 사용 시, chrome browser에서 동일한 request url을 target으로 활용 가능한 tcp connection의 최대 수는 6개이며, 만일 tcp connection 중 하나를 sse를 위해 사용할 시 타 시스템의 정적자원이나, 스크립트 등 기존의 최대 6개를 활용하던 tcp connection 이 5개로 줄어 속도에 영향을 줄 수 있다는 점을 확인해야 한다. http 2 protocol을 활용한 다면 동일 요청지에 대한 무제한적 tcp 요청이 가능하기 때문에 문제가 되지않는다. 
* 더 많은 요청을 처리하기 위해 scale out을 할 경우, load balancing을 어떻게 처리할 지 고민해야 한다. 다만 client의 정보에 따라 sticky한 서비스를 제공하기 보다는 단순히 스트리밍 용의 최대한 stateless 한 용도로만 활용하는 것이 좋아보인다.
* 만일, 다수의 인스턴스를 구동할 때 동시에 특정 이벤트를 발행하고 싶다면 어떻게 해야 할까 고민해봐야 한다. 매 인스턴스 마다 thread pool 과 cpu, memory 등 상황이 다양하기 때문에 정적인 방법으로 동시성을 맞추기엔 무리가 있다. 예를 들어 crontab과 같은 방식으로, http request를 보낼 script를 작성하고, 동시에 scenario에 맞게 실행시키는 방식이 있을 수 있는데, 이는 서블릿 컨테이너의 Http Thread 혹은 connection에 영향에따라 각각의 인스턴스에서 다르게 동작할 위험성이 존재한다. 이를 해결하기 위해서는 spring 에서 제공하는 applicationEventPublisher와 @Async 애노테이션을 활용하여, 비동기적으로 퍼블리싱 되는 이벤트를 Listening 하는 방식이 나아보인다. 예를 들어 카프카를 사용할 경우, 서블릿 컨테이너의 http-thread와는 별개의 thread가 해당 이벤트를 처리하게 되고 특히 따로 해당 처리를 위한 Executor를 @Bean으로 등록시켜 둔다면, 다른 영향도 없이 원활히 동작할 수 있을것이다.




