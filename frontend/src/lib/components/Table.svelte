<script lang="ts">
	import { Paginator, type PaginationSettings, popup } from "@skeletonlabs/skeleton";
	import { FilterIcon, MoreVerticalIcon, PlusIcon, SearchIcon } from "svelte-feather-icons";

	type T = $$Generic;
	type RowAction = {
		text: string;
		icon: any; // I wish I could type this better
		action: () => void;
		destructive?: boolean;
		separated?: boolean;
	};

	export let title: string;
	export let collection: T[];
	export let rowTitle: (item: T) => string;
	export let rowSubtitle: (item: T) => string = () => "";
	export let rowImgSrc: (item: T) => Promise<string> = () => Promise.resolve("");
	export let rowActions: (item: T) => RowAction[] = () => [];
	export let enableAdd = false;

	const popupItem: { [key: string]: T } = {};
	let paginationSettings = {
		page: 0,
		limit: 10,
		size: collection?.length ?? 0,
		amounts: [5, 10]
	} satisfies PaginationSettings;

	$: paginationSettings.size = collection?.length ?? 0;

	$: paginatedSource =
		collection?.slice(
			paginationSettings.page * paginationSettings.limit,
			paginationSettings.page * paginationSettings.limit + paginationSettings.limit
		) ?? [];
</script>

<div class="flex flex-col gap-4">
	<div class="table-container">
		<table class="table table-hover">
			<thead>
				<tr class="ml-4 flex h-10 items-center justify-between">
					<span class="font-bold">
						{title}
					</span>
					<span class="flex gap-0">
						{#if enableAdd}
							<button
								type="button"
								class="btn-icon"
								on:click={() => {
									// TODO: add row to collection
								}}
							>
								<PlusIcon />
							</button>
						{/if}
						<button
							type="button"
							class="btn-icon"
							on:click={() => {
								// TODO: open filter modal, is there a Skeleton filter component?
							}}
						>
							<FilterIcon />
						</button>
						<button
							type="button"
							class="btn-icon"
							on:click={() => {
								// TODO: toggle search input
							}}
						>
							<SearchIcon />
						</button>
					</span>
				</tr>
			</thead>
			<tbody>
				{#if collection.length === 0}
					<tr>
						<td class="text-center italic opacity-50">Nothing to show here!</td>
					</tr>
				{:else}
					{#each paginatedSource as item}
						<tr>
							<td class="flex items-center justify-between">
								<div class="flex items-center gap-4">
									{#await rowImgSrc(item) then imageSrc}
										{#if imageSrc.length > 0}
											<img
												src={imageSrc}
												alt="Avatar of {rowTitle(item)}"
												class="h-8 w-8 shadow-md"
											/>
										{/if}
									{/await}
									<span class="flex flex-col">
										{#if rowTitle}
											<span class="text-lg font-semibold">
												{rowTitle(item)}
											</span>
										{/if}
										{#if rowSubtitle(item).length > 0}
											<span class="opacity-50">
												{rowSubtitle(item)}
											</span>
										{/if}
									</span>
								</div>
								{#if rowActions.length > 0}
									<button
										type="button"
										class="btn-icon btn-icon-sm"
										on:click={() => (popupItem[`rowMenu${title}`] = item)}
										use:popup={{
											event: "click",
											target: `rowMenu${title}`
										}}
									>
										<MoreVerticalIcon />
									</button>
								{/if}
							</td>
						</tr>
					{/each}
				{/if}
			</tbody>
		</table>
	</div>

	<Paginator
		bind:settings={paginationSettings}
		showFirstLastButtons={collection?.length > 30 ?? false}
		showPreviousNextButtons={collection?.length > paginationSettings.limit ?? false}
	/>
</div>

<div class="card z-10 w-fit p-2 shadow-xl" data-popup="rowMenu{title}">
	<div class="bg-surface-100-800-token arrow" />
	<nav class="list-nav">
		<ul>
			{#each rowActions(popupItem[`rowMenu${title}`]).filter(action => !action.separated) as action}
				<li class="child:w-full">
					<button
						type="button"
						class="btn {action.destructive ? 'text-error-500 hover:!bg-error-backdrop-token' : ''}"
						on:click={() => action.action()}
					>
						<span class="badge">
							<svelte:component this={action.icon} />
						</span>
						<span class="flex-auto text-left">{action.text}</span>
					</button>
				</li>
			{/each}
			{#if rowActions(popupItem[`rowMenu${title}`]).filter(action => action.separated).length > 0}
				<hr />
				{#each rowActions(popupItem[`rowMenu${title}`]).filter(action => action.separated) as action}
					<li class="child:w-full">
						<button
							type="button"
							class="btn {action.destructive
								? 'text-error-500 hover:!bg-error-backdrop-token'
								: ''}"
							on:click={() => action.action()}
						>
							<span class="badge">
								<svelte:component this={action.icon} />
							</span>
							<span class="flex-auto text-left">{action.text}</span>
						</button>
					</li>
				{/each}
			{/if}
		</ul>
	</nav>
</div>
