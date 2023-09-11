import type { Server } from "$lib/utils/BackendTypes";
import { localStorageStore } from "@skeletonlabs/skeleton";

export const serversDb = localStorageStore<Server[]>("servers", []);
