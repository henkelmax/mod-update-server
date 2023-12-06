import axios from 'axios';
import { getApiKey } from '@/stores/application';

const IS_DEV_ENVIRONMENT = process.env.NODE_ENV === 'development';

const DEV_BASE_URL='http://localhost:8088';

const instance = axios.create({
  baseURL: IS_DEV_ENVIRONMENT ? DEV_BASE_URL : '',
  timeout: 10000,
  headers: {
    apikey: getApiKey()
  }
});

export default instance;
