const sendRequest = require("../send");

const send = sendRequest(() => {});

test("requisição trivial", async () => {
  const options = {
    method: "POST",
    path: "/echo/post/json",
    headers: {},
    hostname: "reqbin.com",
  };

  const resposta = await send(options);
  const json = JSON.parse(resposta.retorno);
  expect(json.success).toBeTruthy();
});

test("github zen", async () => {
  const options = {
    method: "GET",
    path: "/zen",
    headers: {
      "User-Agent": "kyriosdata",
    },
    hostname: "api.github.com",
  };

  const resposta = await send(options);
  expect(resposta.retorno).toBeTruthy();
});

test("nome de github user", async () => {
  const options = {
    method: "GET",
    path: "/users/kyriosdata",
    headers: {
      "User-Agent": "kyriosdata",
    },
    hostname: "api.github.com",
  };

  const resposta = await send(options);
  const json = JSON.parse(resposta.retorno);
  expect(json.name).toBe("Fábio Nogueira de Lucena");
});
