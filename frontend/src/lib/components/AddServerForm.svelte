<script lang="ts">
	import { DEFAULT_RCON_PORT } from "$lib/utils";
	import type { Server } from "$lib/utils/BackendTypes";
	import { SlideToggle, getModalStore } from "@skeletonlabs/skeleton";

	export let parent: any;

	const modalStore = getModalStore();

	let form: HTMLFormElement;
	let wantsDefaultPort = false;
	const formData: Server = {
		name: "",
		address: "",
		rconPort: NaN,
		rconPassword: "",
		features: {
			rcon: true,
			jmx: true
		}
	};
	let portField: number;
	let storedPort: number;
	let defaultNumber: number;
	$: if (wantsDefaultPort) {
		storedPort = portField;
		portField = DEFAULT_RCON_PORT;
	}
	$: if (!wantsDefaultPort) {
		portField = storedPort;
		storedPort = defaultNumber;
	}

	function submitForm() {
		if (form && !form.reportValidity()) {
			return;
		}
		formData.rconPort = portField;
		if ($modalStore[0].response) {
			$modalStore[0].response(formData);
		}
		modalStore.close();
	}
</script>

{#if $modalStore[0]}
	<div class="card w-modal space-y-4 p-4 shadow-xl">
		<header class="text-2xl font-bold">{$modalStore[0].title ?? "Title"}</header>
		<article>{$modalStore[0].body ?? "Body"}</article>

		<form
			class="space-y-4 border border-surface-500 p-4 rounded-container-token"
			bind:this={form}
			on:submit|preventDefault={submitForm}
		>
			<label class="label">
				<span>Server name</span>
				<input
					class="input"
					type="text"
					bind:value={formData.name}
					placeholder="My server"
					required
				/>
			</label>
			<label class="label">
				<span>Server address</span>
				<input
					class="input"
					type="text"
					bind:value={formData.address}
					placeholder="127.0.0.1, myserver.com"
					required
				/>
			</label>
			<label class="label">
				<span>RCON port</span>
				<div class="flex items-center gap-4">
					<input
						class="input flex-1"
						type="number"
						bind:value={portField}
						min="0"
						max="65535"
						placeholder="Default value: {DEFAULT_RCON_PORT}"
						required={formData.features.rcon}
						disabled={!formData.features.rcon || wantsDefaultPort}
					/>
					<label class="flex items-center gap-2">
						<input type="checkbox" class="checkbox" bind:checked={wantsDefaultPort} />
						<p>Use default port</p>
					</label>
				</div>
			</label>
			<label class="label">
				<span>RCON password</span>
				<input
					class="input"
					type="password"
					bind:value={formData.rconPassword}
					placeholder="Password"
					required={formData.features.rcon}
					disabled={!formData.features.rcon}
				/>
			</label>
			<div class="flex flex-col gap-1">
				<span>Features</span>
				<div class="flex flex-col gap-4">
					<SlideToggle name="rcon-toggle" size="sm" bind:checked={formData.features.rcon}>
						Enable RCON (recommended) - <em class="opacity-75">Enables most Mintstone features</em>
					</SlideToggle>
					<SlideToggle name="jmx-toggle" size="sm" bind:checked={formData.features.jmx} disabled>
						Enable server monitoring - <em class="opacity-75">Not implemented yet</em>
					</SlideToggle>
				</div>
			</div>

			<button hidden />
		</form>

		<footer class={parent.regionFooter}>
			<button class="btn {parent.buttonNeutral}" on:click={parent.onClose}>
				{parent.buttonTextCancel}
			</button>
			<button class="btn {parent.buttonPositive}" on:click={submitForm}>Add server</button>
		</footer>
	</div>
{/if}
