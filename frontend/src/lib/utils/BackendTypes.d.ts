import type { UUID } from "crypto";

export type UpdateCheck =
	| {
			status: "success";
			updateAvailable: false;
	  }
	| {
			status: "success";
			updateAvailable: true;
			latestVersion: string;
			downloadUrl: string;
			changelog: NewVersionChange[];
	  }
	| {
			status: "error";
			message: string;
	  };

type NewVersionChange = {
	version: string;
	changes: string[];
};

export type Version = {
	status: "success";
	platform: Platform;
	version: string;
};

type Platform = "Paper" | "Spigot" | "Bukkit" | "Vanilla" | "Unknown";

export type Server = {
	name: string;
	address: string;
	rconPort: number;
	rconPassword: string;
	features: {
		rcon: boolean;
		jmx: boolean;
	}
};

export type Query = {
	status: "success";
	query: QueryResult;
};

type QueryResult = {
	server: QueryServer;
	version: QueryVersion;
	players: QueryPlayers;
	description: string;
	favicon: string;
	mods?: QueryMods;
	gametype?: string;
	map?: string;
};

type QueryServer = {
	targethostname: string;
	hostname: string;
	ipaddress: string;
	port: number;
	queryport: number;
	latency: number;
};

type QueryVersion = {
	name: string;
	protocol: number;
};

type QueryPlayers = {
	max: number;
	online: number;
	sample?: QueryPlayer[];
};

type QueryPlayer = {
	name: string;
	id: string;
};

type QueryMods = {
	type: string;
	modList: QueryMod[];
};

type QueryMod = {
	modid: string;
	version: string;
};

type Player = WhitelistedPlayer & {
	uuid: UUID;
};

type WhitelistedPlayer = {
	name: string;
};

type BannedEntry = {
	name?: string;
	ip?: string;
	bannedBy: string;
	reason: string;
};
