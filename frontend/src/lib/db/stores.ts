import type { Server } from "$lib/utils/BackendTypes";
import { writable } from "svelte/store";

export const serversDb = writable<Server[]>([]);
