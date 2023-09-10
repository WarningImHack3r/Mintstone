import type { UUID } from "crypto";

// Disable errors due to no TS in markup (https://github.com/sveltejs/language-tools/issues/1026#issuecomment-1002839154)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const disableTypeCheck = (fn: any) => fn;

export const DEFAULT_RCON_PORT = 25575;
export const backendUrl = "http://localhost:8080";

export async function api<T>(route: string, init?: Parameters<typeof fetch>[1]) {
	const response = await fetch(`${backendUrl}${route}`, init);
	if (!response.ok) {
		throw new Error(response.statusText);
	}
	return response.json() as Promise<T>;
}

export async function getMinecraftVersions() {
	const response = await fetch("https://launchermeta.mojang.com/mc/game/version_manifest.json");
	if (!response.ok) {
		throw new Error(response.statusText);
	}
	return (await (response.json() as Promise<{ versions: MinecraftVersion[] }>)).versions.filter(
		version => version.type === "release"
	);
}

async function getMinecraftProtocolVersions() {
	// JSON from https://wiki.vg/Protocol_version_numbers for all versions and their protocol numbers
	const response = await fetch(
		"https://raw.githubusercontent.com/skyrising/mc-versions/main/data/protocol/netty.json"
	);
	if (!response.ok) {
		throw new Error(response.statusText);
	}
	return await (response.json() as Promise<MinecraftProtocolSource>);
}

export async function minecraftVersionFromProtocol(protocol: number) {
	const versions = await getMinecraftProtocolVersions();
	const firstMatch = versions.versions.find(version => version.version === protocol);
	if (!firstMatch) return undefined;
	return firstMatch.clients[firstMatch.clients.length - 1];
}

export async function avatarFromPlayerUUID(uuid: UUID) {
	const response = await fetch(`https://crafatar.com/avatars/${uuid}`);
	if (!response.ok) {
		throw new Error(response.statusText);
	}
	return response.blob();
}

type MinecraftVersion = {
	id: string;
	type: "release" | "snapshot" | "old_beta" | "old_alpha";
	url: string;
	time: string;
	releaseTime: string;
};

type MinecraftProtocolSource = {
	versions: [
		{
			version: number;
			clients: string[];
			servers: string[];
		}
	];
};
