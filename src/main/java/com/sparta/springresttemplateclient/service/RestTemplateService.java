package com.sparta.springresttemplateclient.service;

import com.sparta.springresttemplateclient.dto.ItemDto;
import com.sparta.springresttemplateclient.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RestTemplateService {

    private final RestTemplate restTemplate;

    // RestTemplate 주입 받아오기
    public RestTemplateService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build(); // restteamplte 을 만들 수 있다.
    }

    public ItemDto getCallObject(String query) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                // 서버 입장의 서버에 보낼 준비(7070)
                .fromUriString("http://localhost:7070")
                .path("/api/server/get-call-obj")
                // Request Param 방식
                // controller 에서 받아온 query 넣어 줌
                .queryParam("query", query)
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);

        // get 방식으로 해당 uri 서버(서버 입장의 서버)에 요청, item 에 대한 정보를 ItemDto 로 받는다.
        // restTemplate 두 번째 parameter 로 우리가 변환을 하고 싶은 그 class 의 타입을 주면 자동으로 변환이 되어 넘어온다.
        // ResponseEntity 로 받는다. 앞으로 많이 쓸 것.
        ResponseEntity<ItemDto> responseEntity = restTemplate.getForEntity(uri, ItemDto.class);

        // 서버쪽에서 StatusCode 가 날아온다. -> 성공하면 200 날아옴
        log.info("statusCode = " + responseEntity.getStatusCode());

        return responseEntity.getBody();
    }

    public List<ItemDto> getCallList() {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/get-call-list")
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        log.info("statusCode = " + responseEntity.getStatusCode());
        log.info("Body = " + responseEntity.getBody());

        // getBody 에 String 데이터가 들어있다.
        // ItemDto List 가 반환이 되고 return 되면서 Controller 로 반환됨
        return fromJSONtoItems(responseEntity.getBody());
    }

    public ItemDto postCall(String query) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/post-call/{query}")
                .encode()
                .build()
                .expand(query)
                .toUri();
        log.info("uri = " + uri);

        // 생성자를 사용해 객체를 만든다.
        User user = new User("Robbie", "1234");

        // (uri, HttpBody 에 넣어줄 데이터, 전달받은 데이터랑 Mapping)
        // user 객체로 넣어주면 자동으로 JSON 형태로 변환된다.(restTemplate 이 보낼 때도 자동으로 변환해 줌)
        // ItemDto 로 결과값을 받을 것임
        ResponseEntity<ItemDto> responseEntity = restTemplate.postForEntity(uri, user, ItemDto.class);

        // 결과가 제대로 왔는지 확인
        log.info("statusCode = " + responseEntity.getStatusCode());

        // getBody()를 통해서 그 해당하는 데이터 ItemDto 를 가지고 온다.
        return responseEntity.getBody();
    }

    public List<ItemDto> exchangeCall(String token) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/exchange-call")
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);

        // RequestBody 쪽에도 JSON 데이터 를 보낼 것이기 때문에 User 객체를 만든다.
        User user = new User("Robbie", "1234");

        // RequestEntity 를 사용해서 HTTP 에 들어갈 데이터들을 넣고 만들 수 있다.
        // Request 객체에 담겨온 Header 에 들어있는 데이터 받은 것처럼 서버쪽으로 보낼 때 똑같이 Header 에 담아서 보낸다.
        RequestEntity<User> requestEntity = RequestEntity
                // body 가 있으니까 POST 방식
                .post(uri)
                // key 에는 headerName , value 에는 headerValue 로  받아온 token 을 넣어준다.
                .header("X-Authorization", token)
                // body 부분에 user 넣어준다.
                .body(user);

        // requestEntity 를 첫번 째 파라미터로 전달을 해주면 exchange() 메서드가 실행이 되면서 requestEntity 정보에 따라서 요청이 서버로 넘어간다.
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        return fromJSONtoItems(responseEntity.getBody());
    }

    // String 으로 넘어오는 문자열의 정보를 알아야 한다.
    public List<ItemDto> fromJSONtoItems(String responseEntity) {
        JSONObject jsonObject = new JSONObject(responseEntity);
        // getJSONArray(items) : 데이터를 다 가지고 와서 JSONArray 에 담는다.
        JSONArray items  = jsonObject.getJSONArray("items");
        List<ItemDto> itemDtoList = new ArrayList<>();

        // Object item -> {"title":"Mac","price":3888000} 한 줄씩 뽑힘
        for (Object item : items) {
            // ItemDto 로 변환해준다.
            ItemDto itemDto = new ItemDto((JSONObject)item);
            // 변환된 Dto 를 ItemDto List 에다가 넣는다.
            itemDtoList.add(itemDto);
        }
        // ItemDto 타입의 List 가 반환된다.
        return itemDtoList;
    }
}
