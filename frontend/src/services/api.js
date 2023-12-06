import { axios } from '@/services';

export async function getMods() {
  return (await axios.get('mods')).data;
}

export async function getMod(modId) {
  return (await axios.get(`mods/${modId}`)).data;
}

export async function deleteMod(modId) {
  return (await axios.delete(`mods/${modId}`)).data
}

export async function addMod(mod) {
  return (await axios.post('mods/add', mod)).data
}

export async function editMod(modId, mod) {
  return (await axios.post(`mods/edit/${modId}`, mod)).data
}

export async function getUpdates(modId, amount, page) {
  return (await axios.get(`updates/${modId}?amount=${amount}&page=${page}`)).data;
}

export async function getUpdate(modId, updateId) {
  return (await axios.get(`updates/${modId}/${updateId}`)).data;
}

export async function addUpdate(modId, update) {
  return (await axios.post(`updates/${modId}`, update)).data;
}

export async function editUpdate(modId, updateId, update) {
  return (await axios.post(`updates/${modId}/${updateId}`, update)).data;
}

export async function removeUpdate(modId, updateId) {
  return (await axios.delete(`updates/${modId}/${updateId}`)).data;
}

export async function backup() {
  return (await axios.get('backup')).data;
}

export async function restore(json) {
  return (await axios.post('restore', json)).data;
}

export async function getApiKeys() {
  return (await axios.get('apikeys')).data;
}

export async function deleteApiKey(apiKey) {
  return (await axios.delete(`apikeys/${apiKey}`)).data;
}

export async function addApiKey(mods) {
  return (await axios.post(`apikeys/add`, mods)).data;
}

export function getErrorMessage(err) {
  return err?.response?.data?.message ?? err.message ?? `Unknown error (${err?.response?.status ?? -1})`;
}
