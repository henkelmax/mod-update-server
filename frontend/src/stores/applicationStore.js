export function getApiKey() {
  return sessionStorage.getItem('apiKey');
}

export function setApiKey(apiKey) {
  sessionStorage.setItem('apiKey', apiKey);
}

export function isLoggedIn() {
  return !!getApiKey();
}

export function logout() {
  sessionStorage.removeItem('apiKey');
}

export function skipLogin() {
  setApiKey('00000000-0000-0000-0000-000000000000');
}
